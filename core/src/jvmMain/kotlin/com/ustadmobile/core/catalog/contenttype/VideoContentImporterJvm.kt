package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.catalog.contenttype.media.MediaContentInfo
import com.ustadmobile.core.catalog.contenttype.media.MediaSource
import com.ustadmobile.core.contentjob.AbstractContentImportPlugin
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentPluginUploader
import com.ustadmobile.core.contentjob.DefaultContentPluginUploader
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.newTmpFile
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.HttpPathResponse
import com.ustadmobile.libcache.response.StringResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import java.nio.file.Files
import java.nio.file.Path as NioPath
import kotlin.io.path.absolutePathString
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

class VideoContentImporterJvm(
    endpoint: Endpoint,
    override val di: DI,
    private val cache: UstadCache,
    uriHelper: UriHelper,
    private val ffprobe: FFprobe,
    private val json: Json,
    private val fileSystem: FileSystem = SystemFileSystem,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di),
) : AbstractContentImportPlugin(endpoint, uploader, uriHelper){

    override val pluginId: Int
        get() = 101
    override val supportedMimeTypes: List<String>
        get() = listOf("video/mpeg")

    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    /**
     * This is not responsible for
     */
    override suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.contentJobItem.requireSourceAsDoorUri()
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val videoUrl = "${urlPrefix}video"
        val mediaInfoUrl = "${urlPrefix}media.json"
        val tmpFile = fileSystem.newTmpFile("videoimport", "tmp")

        //Get the mime type from the uri to import if possible
        // If not, try looking at the original filename (might be needed where using temp import
        // files etc.
        val mimeType = uriHelper.getMimeType(jobUri)
            ?: jobItem.contentJobItem?.cjiOriginalFilename?.let {
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

        try {
            uriHelper.openSource(jobUri).use { uriSource ->
                fileSystem.sink(tmpFile).use { fileSink ->
                    uriSource.transferTo(fileSink)
                }
            }

            val contentEntryVersion = ContentEntryVersion(
                cevUid = contentEntryVersionUid,
                cevContentType = ContentEntryVersion.TYPE_VIDEO,
                cevContentEntryUid = jobItem.contentJobItem?.cjiContentEntryUid ?: 0L,
                cevUrl = mediaInfoUrl,
            )

            val videoRequest = requestBuilder(videoUrl)
            val mediaInfoRequest = requestBuilder(mediaInfoUrl)
            cache.store(
                storeRequest = listOf(
                    CacheEntryToStore(
                        request = videoRequest,
                        response = HttpPathResponse(
                            path = tmpFile,
                            fileSystem = fileSystem,
                            mimeType = mimeType,
                            request = videoRequest,
                        )
                    ),
                    CacheEntryToStore(
                        request = mediaInfoRequest,
                        response = StringResponse(
                            request = mediaInfoRequest,
                            mimeType = "application/json",
                            body = json.encodeToString(
                                MediaContentInfo.serializer(), mediaContentInfo,
                            )
                        )
                    )
                )
            )

            contentEntryVersion
        }finally {
            fileSystem.delete(tmpFile)
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
                    pluginId = pluginId,
                    originalFilename = originalFilename,
                )
            }

            throw MalformedContentException("No video stream")
        }catch(e: Throwable) {
            throw MalformedContentException("Exception importing what looked like video", e)
        }finally {
            tmpFile?.deleteRecursively()
        }
    }
}