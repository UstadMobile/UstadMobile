package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.AbstractContentImportPlugin
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentPluginUploader
import com.ustadmobile.core.contentjob.DefaultContentPluginUploader
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.kodein.di.DI
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream

class VideoContentImporterJvm(
    endpoint: Endpoint,
    override val di: DI,
    private val cache: UstadCache,
    uriHelper: UriHelper,
    private val ffprobe: FFprobe,
    private val fileSystem: FileSystem = SystemFileSystem,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di),
) : AbstractContentImportPlugin(endpoint, uploader, uriHelper){



    override val pluginId: Int
        get() = 101
    override val supportedMimeTypes: List<String>
        get() = listOf("video/mpeg")

    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override suspend fun addToCache(
        jobItem: ContentJobItemAndContentJob,
        progressListener: ContentJobProgressListener
    ): ContentEntryVersion {
        TODO("Not yet implemented")
    }

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

        try {
            val tmpFile = Files.createTempFile("ustad-video", "tmp")
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
        }
    }
}