package com.ustadmobile.core.contentformats.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.totalStorageSize
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.tincan.TinCanXML
import java.util.zip.ZipInputStream
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.domain.compress.list.toItemToCompress
import com.ustadmobile.core.domain.compress.originalSizeHeaders
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.libcache.headers.guessByExtensionFromFilename
import com.ustadmobile.libcache.io.unzipTo
import org.xmlpull.v1.XmlPullParserFactory
import kotlinx.coroutines.*
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import java.io.File

class XapiZipContentImporter(
    endpoint: Endpoint,
    private val db: UmAppDatabase,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val json: Json,
    private val tmpPath: Path,
    private val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    private val compressListUseCase: CompressListUseCase,
    private val mimeTypeHelper: MimeTypeHelper,
) : ContentImporter(endpoint) {

    val viewName: String
        get() = XapiContentViewModel.DEST_NAME

    override val supportedMimeTypes: List<String>
        get() = SupportedContent.XAPI_MIME_TYPES

    override val supportedFileExtensions: List<String>
        get() = SupportedContent.ZIP_EXTENSIONS

    override val formatName: String
        get() = "Experience API (TinCan) Zip"

    override val importerId: Int
        get() = PLUGIN_ID

    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?,
    ): MetadataResult? {
        val size = uriHelper.getSize(uri)
        if(size > MAX_SIZE_LIMIT){
            return null
        }

        val mimeType = uriHelper.getMimeType(uri)
        if (mimeType != null && !supportedMimeTypes.contains(mimeType)) {
            return null
        }
        return withContext(Dispatchers.IO) {
            var isTinCan = false
            try {
                return@withContext ZipInputStream(uriHelper.openSource(uri).asInputStream()).use { zipIn ->
                    zipIn.skipToEntry { it.name == TINCAN_FILENAME } ?: return@withContext null
                    isTinCan = true

                    val xppFactory = XmlPullParserFactory.newInstance()
                    val xpp = xppFactory.newPullParser()
                    xpp.setInput(zipIn, "UTF-8")
                    val activity = TinCanXML.loadFromXML(xpp).launchActivity
                        ?: throw IllegalArgumentException("Could not load launch activity")

                    val entry = ContentEntryWithLanguage().apply {
                        contentFlags = ContentEntry.FLAG_IMPORTED
                        licenseType = ContentEntry.LICENSE_TYPE_OTHER
                        title = if (activity.name.isNullOrEmpty())
                            uriHelper.getFileName(uri)
                        else
                            activity.name
                        contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                        description = activity.desc
                        leaf = true
                        entryId = activity.id
                        sourceUrl = uri.uri.toString()
                    }
                    MetadataResult(entry, PLUGIN_ID)
                }
            }catch(e: Throwable) {
                if(isTinCan) {
                    //This is a zip file with a tincan.xml file, but something went wrong.
                    throw InvalidContentException("Invalid tincan.xml: ${e.message}", e)
                }else {
                    throw e
                }
            }
        }
    }

    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener
    ): ContentEntryVersion {
        val jobUri = jobItem.requireSourceAsDoorUri()
        val compressParams = CompressParams(
            compressionLevel = CompressionLevel.forValue(jobItem.cjiCompressionLevel)
        )

        val tinCanEntry = ZipInputStream(
            uriHelper.openSource(jobUri).asInputStream()
        ).use { zipIn ->
            zipIn.skipToEntry { it.name == TINCAN_FILENAME }
        } ?: throw FatalContentJobException("XapiImportPlugin: no tincan entry file")

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextIdAsync(
            ContentEntryVersion.TABLE_ID)

        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val manifestUrl = "$urlPrefix${ContentConstants.MANIFEST_NAME}"

        val workTmpPath = Path(tmpPath, "xapi-import-${systemTimeInMillis()}")
        val xapiZipEntries = uriHelper.openSource(jobUri).use { zipSource ->
            zipSource.unzipTo(workTmpPath)
        }

        val compressedEntries = compressListUseCase(
            items = xapiZipEntries.map {
                it.toItemToCompress(mimeType = mimeTypeHelper.guessByExtensionFromFilename(it.name))
            },
            params = compressParams,
            workDir = tmpPath,
            onProgress = {
                progressListener.onProgress(
                    jobItem.copy(
                        cjiItemTotal = it.total,
                        cjiItemProgress = it.completed
                    )
                )
            }
        )

        return try {
            val xapiManifestEntries = saveLocalUriAsBlobAndManifestUseCase(
                items = compressedEntries.map { compressListResult ->
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = compressListResult.localUri,
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = compressListResult.mimeType,
                            extraHeaders = compressListResult.compressedResult.originalSizeHeaders(),
                        ),
                        manifestUri = compressListResult.originalItem.name,
                        manifestMimeType = compressListResult.mimeType,
                    )
                }
            )

            val manifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = xapiManifestEntries.map { it.manifestEntry }
            )

            cache.storeText(
                url = manifestUrl,
                text = json.encodeToString(ContentManifest.serializer(), manifest),
                mimeType = "application/json",
                cacheControl = "immutable"
            )

            ContentEntryVersion(
                cevUid = contentEntryVersionUid,
                cevContentType = ContentEntryVersion.TYPE_XAPI,
                cevContentEntryUid = jobItem.cjiContentEntryUid,
                cevManifestUrl = manifestUrl,
                cevOpenUri = tinCanEntry.name,
                cevStorageSize = xapiManifestEntries.totalStorageSize(),
                cevOriginalSize = uriHelper.getSize(jobUri)
            )
        }finally {
            File(workTmpPath.toString()).deleteRecursively()
        }
    }

    companion object {

        const val TINCAN_FILENAME = "tincan.xml"

        const val PLUGIN_ID = 8

        private const val MAX_SIZE_LIMIT: Long = 500 * 1024 * 1024 //500MB

    }
}