package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class VideoTypePluginJvm(
    context: Any,
    endpoint: Endpoint,
    override val di: DI,
    uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): ContentImportContentPlugin(endpoint, context, uploader) {

    val viewName: String
        get() = VideoContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = VIDEO_MIME_MAP.keys.toList()

    override val supportedFileExtensions: List<String>
        get() = VideoContentPresenterCommon.VIDEO_EXT_LIST.map { it.removePrefix(".") }

    override val pluginId: Int
        get() = VideoTypePlugin.PLUGIN_ID

    private val VIDEO_JVM = "VideoPluginJVM"

    private val ffmpegFile: File by di.instance(tag = DiTag.TAG_FILE_FFMPEG)

    private val ffprobeFile: File by di.instance(tag = DiTag.TAG_FILE_FFPROBE)

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun makeContainer(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progressListener: ContentJobProgressListener,
        containerStorageUri: DoorUri,
    ) : Container{
        val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        val videoUri = jobItem.contentJobItem?.sourceUri?.let {DoorUri.parse(it) }
            ?: throw IllegalArgumentException("No source uri!")
        val localVideoUri = process.getLocalOrCachedUri()
        val videoFile = localVideoUri.toFile()
        var pathInContainer = if(!videoUri.isRemote()) {
            videoFile.name
        }else {
            val extension = videoUri.guessMimeType(context, di)?.let { mimeType ->
                VIDEO_MIME_MAP[mimeType]
            } ?: throw IllegalArgumentException("Unknown mime type for $videoUri")

            videoUri.getFileName(context).requirePostfix(extension)
        }

        val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

        Napier.d(tag = VIDEO_JVM, message = "conversion Params compress video is $compressVideo")

        var videoFileToAddToContainer = videoFile
        if (compressVideo) {
            videoFileToAddToContainer = File(videoFile.parentFile,
                "new${videoFile.nameWithoutExtension}.mp4")
            val fileVideoDimensionsAndAspectRatio = ShrinkUtils.getVideoResolutionMetadata(
                videoFile, ffprobeFile)
            val newVideoDimensions = Pair(fileVideoDimensionsAndAspectRatio.first,
                fileVideoDimensionsAndAspectRatio.second).fitWithin()
            ShrinkUtils.optimiseVideo(videoFile, videoFileToAddToContainer, ffmpegFile,
                newVideoDimensions, fileVideoDimensionsAndAspectRatio.third)
            pathInContainer = "new${videoFile.nameWithoutExtension}.mp4"
        }

        val container = repo.containerBuilder(jobItem.contentJobItem?.cjiContentEntryUid ?: 0,
                supportedMimeTypes.first(), containerStorageUri)
            .addFile(pathInContainer, videoFileToAddToContainer, ContainerBuilder.Compression.NONE)
            .build()

        videoFile.delete()
        videoFileToAddToContainer.delete()

        return container
    }


    suspend fun getEntry(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default){

            val localUri = process.getLocalOrCachedUri()

            val fileName = uri.getFileName(context)

            if(!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val file = localUri.toFile()

            val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(file, ffprobeFile)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            val entry = ContentEntryWithLanguage().apply {
                this.title = fileName
                this.leaf = true
                sourceUrl = uri.uri.toString()
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry, PLUGIN_ID)
        }
    }

    companion object {
        const val PLUGIN_ID = 12
    }

}