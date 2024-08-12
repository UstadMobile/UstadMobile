package com.ustadmobile.core.contentformats.video

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.contentformats.media.MediaSource
import com.ustadmobile.core.contentformats.media.VideoConstants
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.compress.originalSizeHeaders
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.domain.extractvideothumbnail.ExtractVideoThumbnailUseCase
import com.ustadmobile.core.domain.validatevideofile.ValidateVideoFileUseCase
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.fileExtensionOrNull
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.MimeTypeHelper
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * https://github.com/caprica/vlcj-examples/blob/master/src/main/java/uk/co/caprica/vlcj/test/snapshot/SnapshotTest.java
 */
class VideoContentImporterCommonJvm(
    endpoint: Endpoint,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val json: Json,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val db: UmAppDatabase,
    private val tmpPath: Path,
    private val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
    private val validateVideoFileUseCase: ValidateVideoFileUseCase,
    private val mimeTypeHelper: MimeTypeHelper,
    private val compressUseCase: CompressVideoUseCase? = null,
    private val extractVideoThumbnailUseCase: ExtractVideoThumbnailUseCase? = null,
    private val saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase? = null,
) : ContentImporter(endpoint) {


    override val importerId: Int
        get() = IMPORTER_ID
    override val supportedMimeTypes: List<String>
        get() = listOf("video/mpeg")

    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override val formatName: String
        get() = "Video(MP4, M4V, Quicktime, WEBM, OGV, AVI)"

    /**
     * This is not responsible for checking that this is actually a video file, that is done by
     * extractmetadata.
     */
    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.requireSourceAsDoorUri()
        val fromUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(jobUri)

        //Get the mime type from the uri to import if possible
        // If not, try looking at the original filename (might be needed where using temp import
        // files etc.
        val fromMimeType = uriHelper.getMimeType(jobUri)
            ?: jobItem.cjiOriginalFilename?.fileExtensionOrNull()?.let {
                mimeTypeHelper.guessByExtension(it)
            } ?: throw IllegalStateException("Cannot get mime type")


        val compressUseCaseVal = compressUseCase
        val compressionLevel = CompressionLevel.forValue(jobItem.cjiCompressionLevel)
        val originalSize = uriHelper.getSize(fromUri)
        val compressionResult = compressUseCaseVal?.takeIf {
            compressionLevel != CompressionLevel.NONE
        }?.invoke(
            fromUri = fromUri.toString(),
            toUri = null,
            onProgress = {
                progressListener.onProgress(
                    jobItem.copy(
                        cjiItemTotal = it.total,
                        cjiItemProgress = it.completed
                    )
                )
            },
            params = CompressParams(
                compressionLevel = compressionLevel,
            )
        )

        val (uri, mimeType) = if(compressionResult != null) {
            Pair(DoorUri.parse(compressionResult.uri), compressionResult.mimeType)
        }else {
            Pair(fromUri, fromMimeType)
        }

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val manifestUrl = "$urlPrefix${ContentConstants.MANIFEST_NAME}"
        val videoEntryUri = "video"
        val mediaInfoEntryUri = "media.json"
        val workDir = Path(tmpPath, "video-import-${systemTimeInMillis()}")
        fileSystem.createDirectories(workDir)

        val mediaContentInfo = MediaContentInfo(
            sources = listOf(
                MediaSource(
                    uri = videoEntryUri,
                    mimeType = mimeType
                )
            )
        )

        val mediaInfoTmpFile = Path(workDir, "media.json")
        fileSystem.sink(mediaInfoTmpFile).buffered().use {
            it.writeString(
                json.encodeToString(
                    MediaContentInfo.serializer(), mediaContentInfo,
                )
            )
        }

        try {
            val savedManifestItems = saveLocalUriAsBlobAndManifestUseCase(
                listOf(
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = uri.toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = mimeType,
                            deleteLocalUriAfterSave = false,
                            extraHeaders = compressionResult.originalSizeHeaders(),
                        ),
                        manifestUri = videoEntryUri
                    ),
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = mediaInfoTmpFile.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = "application/json",
                            deleteLocalUriAfterSave = true,
                        ),
                        manifestUri = mediaInfoEntryUri,
                    )
                )
            )

            //Now that the video has been saved as a local URI, delete the temporary copy
            compressionResult?.uri?.takeIf { it.startsWith("file:") }
                ?.let { DoorUri.parse(it) }?.toFile()?.delete()

            val manifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = savedManifestItems.map { it.manifestEntry }
            )

            cache.storeText(
                url = manifestUrl,
                text = json.encodeToString(ContentManifest.serializer(), manifest),
                mimeType = "application/json",
                cacheControl = "immutable"
            )


            ContentEntryVersion(
                cevUid = contentEntryVersionUid,
                cevContentType = ContentEntryVersion.TYPE_VIDEO,
                cevContentEntryUid = jobItem.cjiContentEntryUid,
                cevManifestUrl = manifestUrl,
                cevOpenUri = mediaInfoEntryUri,
                cevOriginalSize = originalSize,
                cevStorageSize = compressionResult?.compressedSize ?: originalSize,
            )
        }finally {
            File(workDir.toString()).deleteRecursively()
        }
    }

    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?
    ): MetadataResult? = withContext(Dispatchers.IO) {
        val hasVideoExtension = originalFilename?.substringAfterLast(".")?.lowercase()?.let {
            it in VideoConstants.VIDEO_EXT_LIST
        } ?: false

        //Check if this looks like it should be a video file
        if(!hasVideoExtension && uriHelper.getMimeType(uri)?.startsWith("video/") != true) {
            return@withContext null
        }

        try {
            val localUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(uri)
            val hasVideo = validateVideoFileUseCase(localUri)

            if(hasVideo) {
                val thumbDestPath = Path(tmpPath, "video-import-thumbnail-${systemTimeInMillis()}.png")
                val videoThumbnailUri = if(
                    extractVideoThumbnailUseCase != null && saveLocalUrisAsBlobsUseCase != null
                ) {
                    try {
                        val thumbnailResult = extractVideoThumbnailUseCase.invoke(
                            videoUri = uri,
                            position = 0.3f,
                            destinationFilePath = thumbDestPath.toString()
                        )
                        saveLocalUrisAsBlobsUseCase.invoke(
                            listOf(
                                SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                                    localUri = thumbnailResult.uri.toString(),
                                    mimeType = thumbnailResult.mimeType,
                                )
                            )
                        ).firstOrNull()?.blobUrl
                    }catch(e: Throwable){
                        null
                    }
                }else {
                    null
                }


                return@withContext MetadataResult(
                    entry = ContentEntryWithLanguage().apply {
                        title = originalFilename ?: uri.toString().substringAfterLast("/")
                            .substringBefore("?")
                        leaf = true
                        sourceUrl = uri.toString()
                        contentTypeFlag = ContentEntry.TYPE_VIDEO
                    },
                    importerId = importerId,
                    originalFilename = originalFilename,
                    picture = videoThumbnailUri?.let {
                        ContentEntryPicture2(
                            cepPictureUri = it,
                            cepThumbnailUri = it,
                        )
                    }
                )
            }

            throw InvalidContentException("No video stream")
        }catch(e: Throwable) {
            Napier.w(throwable = e) { "Exception importing what looked like video: $e" }
            throw InvalidContentException("Exception importing what looked like video", e)
        }
    }

    companion object {

        const val IMPORTER_ID = 101
    }
}