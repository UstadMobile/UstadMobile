package com.ustadmobile.core.contentformats.video

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.contentformats.media.MediaSource
import com.ustadmobile.core.contentformats.media.VideoConstants
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path as NioPath
import kotlin.io.path.absolutePathString
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

class VideoContentImporterJvm(
    endpoint: Endpoint,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val ffprobe: FFprobe,
    private val json: Json,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val db: UmAppDatabase,
    private val tmpPath: Path,
    private val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
): ContentImporter(
    endpoint = endpoint
) {

    override val importerId: Int
        get() = 101
    override val supportedMimeTypes: List<String>
        get() = listOf("video/mpeg")

    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override val formatName: String
        get() = "Video(MP4, M4V, Quicktime, WEBM, OGV, AVI)"

    /**
     * This is not responsible for
     */
    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.requireSourceAsDoorUri()

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val manifestUrl = "$urlPrefix${ContentConstants.MANIFEST_NAME}"
        val videoUrl = "${urlPrefix}video"
        val mediaInfoUrl = "${urlPrefix}media.json"
        val workDir = Path(tmpPath, "video-import-${systemTimeInMillis()}")
        fileSystem.createDirectories(workDir)

        val videoTmpFile = Path(workDir, "video")

        //Get the mime type from the uri to import if possible
        // If not, try looking at the original filename (might be needed where using temp import
        // files etc.
        val mimeType = uriHelper.getMimeType(jobUri)
            ?: jobItem.cjiOriginalFilename?.let {
                uriHelper.getMimeType(DoorUri.parse("file:///$it"))
            } ?: throw IllegalStateException("Cannot get mime type")

        val mediaContentInfo = MediaContentInfo(
            sources = listOf(
                MediaSource(
                    url = videoUrl,
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
            uriHelper.openSource(jobUri).use { uriSource ->
                fileSystem.sink(videoTmpFile).use { fileSink ->
                    uriSource.transferTo(fileSink)
                }
            }

            val savedManifestItems = saveLocalUriAsBlobAndManifestUseCase(
                listOf(
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = videoTmpFile.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = mimeType,
                            deleteLocalUriAfterSave = true,
                        ),
                        manifestUri = "video"
                    ),
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = mediaInfoTmpFile.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = "application/json",
                            deleteLocalUriAfterSave = true,
                        ),
                        manifestUri = "media.json"
                    )
                )
            )

            val manifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = savedManifestItems.map { it.manifestEntry }
            )

            cache.storeText(
                url = manifestUrl,
                text = json.encodeToString(ContentManifest.serializer(), manifest),
                mimeType = "application/json"
            )


            ContentEntryVersion(
                cevUid = contentEntryVersionUid,
                cevContentType = ContentEntryVersion.TYPE_VIDEO,
                cevContentEntryUid = jobItem.cjiContentEntryUid,
                cevSitemapUrl = manifestUrl,
                cevUrl = mediaInfoUrl,
            )
        }finally {
            File(workDir.toString()).deleteRecursively()
        }
    }

    @OptIn(ExperimentalPathApi::class)
    @Suppress("NewApi") //This is JVM only
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

        var tmpFile: NioPath? = null

        try {
            tmpFile = Files.createTempFile("ustad-video", "tmp")
            val tmpFilePath = Path(tmpFile.absolutePathString())
            uriHelper.openSource(uri).use { source ->
                fileSystem.sink(tmpFilePath).use { sink ->
                    source.transferTo(sink)
                }
            }

            val probeResult = ffprobe.probe(tmpFilePath.toString())
            val hasVideo = probeResult.getStreams().any {
                it.codec_type == FFmpegStream.CodecType.VIDEO
            }

            if(hasVideo) {
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
                )
            }

            throw InvalidContentException("No video stream")
        }catch(e: Throwable) {
            throw InvalidContentException("Exception importing what looked like video", e)
        }finally {
            tmpFile?.deleteRecursively()
        }
    }
}