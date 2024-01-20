package com.ustadmobile.core.contentformats.video

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.media.VideoConstants
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegStream
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import java.nio.file.Path as NioPath

class VideoContentImporterJvm(
    endpoint: Endpoint,
    cache: UstadCache,
    uriHelper: UriHelper,
    private val ffprobe: FFprobe,
    json: Json,
    fileSystem: FileSystem = SystemFileSystem,
    db: UmAppDatabase,
    tmpPath: Path,
    saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
): AbstractVideoContentImporterCommonJvm(
    endpoint = endpoint,
    cache = cache,
    uriHelper = uriHelper,
    json = json,
    fileSystem = fileSystem,
    db = db,
    tmpPath = tmpPath,
    saveLocalUriAsBlobAndManifestUseCase = saveLocalUriAsBlobAndManifestUseCase,
) {

    override val importerId: Int
        get() = 101
    override val supportedMimeTypes: List<String>
        get() = listOf("video/mpeg")

    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override val formatName: String
        get() = "Video(MP4, M4V, Quicktime, WEBM, OGV, AVI)"


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