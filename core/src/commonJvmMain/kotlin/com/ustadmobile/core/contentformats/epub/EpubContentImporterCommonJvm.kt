package com.ustadmobile.core.contentformats.epub

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.epub.ocf.Container
import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.totalStorageSize
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.epub.GetEpubTableOfContentsUseCase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.substringUntilLastIndexOfInclusive
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.unzipTo
import com.ustadmobile.libcache.uuid.randomUuid
import io.github.aakira.napier.Napier
import java.util.zip.ZipInputStream
import kotlinx.coroutines.*
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File
import java.net.URLDecoder
import java.util.zip.ZipEntry

class EpubContentImporterCommonJvm(
    endpoint: Endpoint,
    private val db: UmAppDatabase,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val xml: XML,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val xhtmlFixer: XhtmlFixer,
    private val getEpubTableOfContentsUseCase: GetEpubTableOfContentsUseCase =
        GetEpubTableOfContentsUseCase(xml),
    private val tmpPath: Path,
    private val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    private val json: Json,
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
    private val compressListUseCase: CompressListUseCase,
    private val saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase? = null,
) : ContentImporter(endpoint) {

    val viewName: String
        get() = EpubContentViewModel.DEST_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.EPUB_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.EPUB_EXTENSIONS

    override val importerId: Int
        get() = PLUGIN_ID

    override val formatName: String
        get() = "EPUB"

    private fun ZipInputStream.findFirstOpfPath(): String? {
        skipToEntry { entry -> entry.name == OCF_CONTAINER_PATH } ?: return null

        val container = xml.decodeFromString(
            deserializer = Container.serializer(),
            string = readString()
        )

        return container.rootFiles?.rootFiles?.firstOrNull()?.fullPath
    }

    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?,
    ): MetadataResult? {
        val mimeType = uriHelper.getMimeType(uri)

        if(!((mimeType != null && mimeType in supportedMimeTypes) ||
            originalFilename?.substringAfterLast(".")?.lowercase() == "epub")
        ) {
            return null
        }

        val localUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(uri)

        return withContext(Dispatchers.IO) {
            try {
                val opfPath = ZipInputStream(
                    uriHelper.openSource(localUri).asInputStream()
                ).use {
                    it.findFirstOpfPath()
                } ?: throw IllegalStateException("Container.xml not found in EPUB / cant find path for opf")

                val entryNames = mutableListOf<String>()
                var opfPackage: PackageDocument? = null

                ZipInputStream(uriHelper.openSource(localUri).asInputStream()).use { zipIn ->
                    lateinit var zipEntry: ZipEntry
                    while(zipIn.nextEntry?.also { zipEntry = it } != null) {
                        entryNames += zipEntry.name
                        if(zipEntry.name == opfPath) {
                            opfPackage = xml.decodeFromString(zipIn.bufferedReader().readText())
                        }
                    }
                }

                val opfPackageVal = opfPackage
                    ?: throw IllegalStateException("epub does not contain opf: expected to find: $opfPath")

                fun itemPathInZip(href: String): String {
                    val hrefDecoded = URLDecoder.decode(href, "UTF-8")
                    return opfPath.substringUntilLastIndexOfInclusive("/", "") + hrefDecoded
                }

                /*
                 * As per https://www.w3.org/submissions/2017/SUBM-epub-packages-20170125/ section
                 * 5.1 The EPUB Navigation Document is a mandatory component of an EPUB Package.
                 * Therefor any epub without a table of contents is NOT valid.
                 */
                getEpubTableOfContentsUseCase(
                    opfPackage = opfPackageVal,
                    readItemText = { item ->
                        val pathInZip = itemPathInZip(item.href)
                        ZipInputStream(uriHelper.openSource(localUri).asInputStream()).use { zipIn ->
                            if(zipIn.skipToEntry { it.name == pathInZip } != null)
                                zipIn.bufferedReader().readText()
                            else
                                throw IllegalArgumentException("$pathInZip not found for ${item.href}")
                        }
                    }
                ) ?: throw IllegalStateException("EPUB table of contents/nav document not found.")

                //go over manifest - make sure that all items are present
                val missingItems = opfPackageVal.manifest.items.filter { item ->
                    itemPathInZip(item.href) !in entryNames
                }

                if(missingItems.isNotEmpty()) {
                    throw IllegalStateException("Item(s) from manifest are missing: " +
                            missingItems.joinToString { it.href })
                }

                val coverImg = opfPackageVal.coverItem()
                val coverImgBlob = if(coverImg != null) {
                    val opfBasePath = opfPath.substringUntilLastIndexOfInclusive("/", "")
                    val coverTmpPath = Path(tmpPath, randomUuid())
                    fileSystem.takeIf { !it.exists(tmpPath) }?.createDirectories(tmpPath)

                    val unzippedCoverPath = ZipInputStream(uriHelper.openSource(localUri).asInputStream()).use { zipIn ->
                        val coverItemZipEntry = zipIn.skipToEntry {
                            it.name == opfBasePath + coverImg.href
                        }

                        if(coverItemZipEntry != null) {
                            fileSystem.sink(coverTmpPath).buffered().asOutputStream().use { coverTmpOut ->
                                zipIn.copyTo(coverTmpOut)
                                coverTmpOut.close()
                            }

                            coverTmpPath
                        }else {
                            null
                        }
                    }

                    unzippedCoverPath?.let { path ->
                        saveLocalUrisAsBlobsUseCase?.invoke(
                            listOf(
                                SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                    localUri = File(path.toString()).toDoorUri().toString(),
                                    mimeType = coverImg.mediaType,
                                )
                            )
                        )?.firstOrNull()
                    }?.also {
                        fileSystem.delete(coverTmpPath, mustExist = false)
                    }
                }else {
                    null
                }

                val entry = ContentEntryWithLanguage().apply {
                    contentFlags = ContentEntry.FLAG_IMPORTED
                    contentTypeFlag = ContentEntry.TYPE_EBOOK
                    licenseType = ContentEntry.LICENSE_TYPE_OTHER
                    title = opfPackageVal.metadata.titles.firstOrNull()?.content?.let {
                        it.ifBlank { null }
                    } ?: uriHelper.getFileName(uri)
                    author = opfPackageVal.metadata.creators.joinToString { it.content }
                    description = opfPackageVal.metadata.descriptions.firstOrNull()?.content ?: ""
                    leaf = true
                    sourceUrl = uri.uri.toString()
                    entryId = opfPackageVal.uniqueIdentifierContent()
                    val languageCode = opfPackageVal.metadata.languages.firstOrNull()?.content
                    if (languageCode != null) {
                        this.language = Language().apply {
                            iso_639_1_standard = languageCode
                        }
                    }
                }
                val picture = coverImgBlob?.let {
                    ContentEntryPicture2(
                        cepPictureUri = it.blobUrl,
                        cepThumbnailUri = it.blobUrl,
                    )
                }

                return@withContext MetadataResult(
                    entry = entry,
                    importerId = PLUGIN_ID,
                    picture = picture,
                )
            } catch (e: Exception) {
                throw InvalidContentException("Invalid epub: ${e.message}", e)
            }
        }
    }


    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.sourceUri?.let { DoorUri.parse(it) }
            ?: throw IllegalArgumentException("no sourceUri")
        val localUri =  getStoragePathForUrlUseCase.getLocalUriIfRemote(jobUri)
        val compressParams = CompressParams(
            compressionLevel = CompressionLevel.forValue(jobItem.cjiCompressionLevel)
        )

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val manifestUrl = "$urlPrefix${ContentConstants.MANIFEST_NAME}"

        val opfPath = ZipInputStream(uriHelper.openSource(localUri).asInputStream()).use {
            it.findFirstOpfPath()
        } ?: throw IllegalArgumentException("No OPF found")

        val workPath = Path(tmpPath, "epub-import-${systemTimeInMillis()}")

        uriHelper.openSource(localUri).use { zipSource ->
            val unzippedEntries = zipSource.unzipTo(workPath)
            val opfEntry = unzippedEntries.first { it.name == opfPath }
            val opfStr = fileSystem.source(opfEntry.path).buffered().readString()

            val opfPackage = xml.decodeFromString(PackageDocument.serializer(), opfStr)

            try {
                val opfManifestItems = opfPackage.manifest.items.map { opfItem ->
                    val hrefDecoded = URLDecoder.decode(opfItem.href, "UTF-8")
                    val opfBasePath = opfEntry.name.substringUntilLastIndexOfInclusive("/", "")
                    val pathInZip = opfBasePath + hrefDecoded

                    val unzippedEntry = unzippedEntries.firstOrNull {
                        it.name == pathInZip
                    } ?: throw IllegalArgumentException("Cannot find $pathInZip")

                    /* If this is XHTML, then use the xhtmlFixer to check for invalid XHTML
                     * content (some content e.g. Storyweaver includes br's without the trailing
                     * slash etc).
                     */
                    if (opfItem.mediaType == "application/xhtml+xml") {
                        val xhtmlText =
                            fileSystem.source(unzippedEntry.path).buffered().use { fileSource ->
                                fileSource.readString()
                            }

                        val fixResult = xhtmlFixer.fixXhtml(xhtmlText)
                        if (!fixResult.wasValid) {
                            fileSystem.sink(unzippedEntry.path).buffered().use {
                                it.writeString(fixResult.xhtml)
                            }
                        }
                    }

                    CompressListUseCase.ItemToCompress(
                        path = unzippedEntry.path,
                        name = unzippedEntry.name,
                        mimeType = opfItem.mediaType,
                    )
                }

                val compressedItems = compressListUseCase(
                    items = opfManifestItems,
                    params = compressParams,
                    workDir = workPath,
                    onProgress = {
                        progressListener.onProgress(
                            jobItem.copy(
                                cjiItemTotal = it.total,
                                cjiItemProgress = it.completed
                            )
                        )
                    }
                )

                val manifestedItems = saveLocalUriAsBlobAndManifestUseCase(
                    items = compressedItems.map {
                        SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                            blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                localUri = it.localUri,
                                entityUid = contentEntryVersionUid,
                                tableId = ContentEntryVersion.TABLE_ID,
                                mimeType = it.mimeType,
                            ),
                            manifestUri = it.originalItem.name,
                            manifestMimeType = it.mimeType,
                        )
                    } + SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = opfEntry.path.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = "application/oebps-package+xml",
                        ),
                        manifestUri = opfPath
                    )
                )

                val manifest = ContentManifest(
                    version = 1,
                    metadata = emptyMap(),
                    entries = manifestedItems.map { it.manifestEntry }
                )

                cache.storeText(
                    url = manifestUrl,
                    text = json.encodeToString(ContentManifest.serializer(), manifest),
                    mimeType = "application/json"
                )

                ContentEntryVersion(
                    cevUid = contentEntryVersionUid,
                    cevContentType = ContentEntryVersion.TYPE_EPUB,
                    cevContentEntryUid = jobItem.cjiContentEntryUid,
                    cevManifestUrl = manifestUrl,
                    cevOpenUri = opfPath,
                    cevOriginalSize = uriHelper.getSize(jobUri),
                    cevStorageSize = manifestedItems.totalStorageSize(),
                )
            }catch(e: Exception) {
                Napier.e("EpubTypePlugin: Exception importing epub", e)
                throw e
            }finally {
                File(workPath.toString()).deleteRecursively()
            }
        }
    }

    companion object {

        private const val OCF_CONTAINER_PATH = "META-INF/container.xml"

        const val PLUGIN_ID = 2
    }
}
