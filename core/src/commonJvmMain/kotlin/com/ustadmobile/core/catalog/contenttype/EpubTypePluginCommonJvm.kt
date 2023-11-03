package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.epub.ocf.Container
import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.CouponHeader
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.io.unzipTo
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import java.util.zip.ZipInputStream
import kotlinx.coroutines.*
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class EpubTypePluginCommonJvm(
    endpoint: Endpoint,
    override val di: DI,
    private val cache: UstadCache,
    uriHelper: UriHelper,
    private val xml: XML,
    private val fileSystem: FileSystem = SystemFileSystem,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : AbstractContentImportPlugin(endpoint, uploader, uriHelper) {

    val viewName: String
        get() = EpubContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.EPUB_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.EPUB_EXTENSIONS

    override val pluginId: Int
        get() = PLUGIN_ID

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
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {
            try {
                val opfPath = ZipInputStream(uriHelper.openSource(uri).asInputStream()).use {
                    it.findFirstOpfPath()
                } ?: return@withContext null

                return@withContext  ZipInputStream(uriHelper.openSource(uri).asInputStream()).use {

                    it.skipToEntry { it.name == opfPath } ?: return@use null

                    val packageStr = it.readString()

                    val opfPackage: PackageDocument = xml.decodeFromString(packageStr)

                    val entry = ContentEntryWithLanguage().apply {
                        contentFlags = ContentEntry.FLAG_IMPORTED
                        contentTypeFlag = ContentEntry.TYPE_EBOOK
                        licenseType = ContentEntry.LICENSE_TYPE_OTHER
                        title = opfPackage.metadata.titles.firstOrNull()?.content?.let {
                            it.ifBlank { null }
                        } ?: uriHelper.getFileName(uri)
                        author = opfPackage.metadata.creators.joinToString { it.content }
                        description = opfPackage.metadata.descriptions.firstOrNull()?.content ?: ""
                        leaf = true
                        sourceUrl = uri.uri.toString()
                        entryId = opfPackage.uniqueIdentifierContent()
                        val languageCode = opfPackage.metadata.languages.firstOrNull()?.content
                        if (languageCode != null) {
                            this.language = Language().apply {
                                iso_639_1_standard = languageCode
                            }
                        }
                    }

                    return@use MetadataResult(entry, PLUGIN_ID)
                }
            } catch (e: Exception) {
                null
            }
        }
    }


    override suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.contentJobItem?.sourceUri?.let { DoorUri.parse(it) }
            ?: throw IllegalArgumentException("no sourceUri")
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)

        val opfPath = ZipInputStream(uriHelper.openSource(jobUri).asInputStream()).use {
            it.findFirstOpfPath()
        } ?: throw IllegalArgumentException("No OPF found")

        val contentEntryVersion = ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_EPUB,
            cevContentEntryUid = jobItem.contentJobItem?.cjiContentEntryUid ?: 0L,
            cevUrl = "$urlPrefix$opfPath",
        )

        val tmpDir = File.createTempFile("epubimport", "tmp").also {
            it.delete()
            it.mkdir()
        }
        val tmpPath = Path(tmpDir.absolutePath)

        uriHelper.openSource(jobUri).use { zipSource ->
            val unzippedEntries = zipSource.unzipTo(tmpPath)
            val opfEntry = unzippedEntries.first { it.name == opfPath }
            val opfStr = fileSystem.source(opfEntry.path).buffered().readString()

            val opfPackage = xml.decodeFromString(PackageDocument.serializer(), opfStr)
            val opfUrl = UMFileUtil.resolveLink(urlPrefix, opfPath)

            try {
                cache.store(
                    opfPackage.manifest.items.map {
                        val request = requestBuilder(UMFileUtil.resolveLink(opfUrl, it.href))
                        val pathInZip = Path(opfEntry.name).parent?.let { opfParent ->
                            Path(opfParent, it.href)
                        } ?: Path(it.href)

                        CacheEntryToStore(
                            request = request,
                            response = HttpPathResponse(
                                path = unzippedEntries.first { it.name == pathInZip.toString() }.path,
                                fileSystem = fileSystem,
                                mimeType = it.mediaType,
                                request = request,
                                extraHeaders = headersBuilder {
                                    header(CouponHeader.COUPON_STATIC, "true")
                                }
                            )
                        )
                    } + listOf(
                        requestBuilder(opfUrl).let {
                            CacheEntryToStore(
                                request = it,
                                response = HttpPathResponse(
                                    path = opfEntry.path,
                                    fileSystem = fileSystem,
                                    mimeType = "application/oebps-package+xml",
                                    request = it,
                                )
                            )
                        }
                    )
                )
            }catch(e: Exception) {
                Napier.e("EpubTypePlugin: Exception caching epub", e)
                throw e
            }finally {
                tmpDir.deleteRecursively()
                tmpDir.takeIf { it.exists() }?.deleteOnExit()
            }
        }

        contentEntryVersion
    }

    companion object {

        private const val OCF_CONTAINER_PATH = "META-INF/container.xml"

        const val PLUGIN_ID = 2
    }
}
