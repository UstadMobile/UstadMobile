package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import io.github.aakira.napier.Napier
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.lang.IllegalArgumentException

class VideoTypePluginJvm(private var context: Any, private val endpoint: Endpoint, override val di: DI): VideoTypePlugin() {

    private val VIDEO_JVM = "VideoPluginJVM"

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    override suspend fun canProcess(doorUri: DoorUri, process: ProcessContext): Boolean {
        return getEntry(doorUri, process) != null
    }

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): ContentEntryWithLanguage? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext): ProcessResult {
        withContext(Dispatchers.Default) {

            val uri = jobItem.fromUri ?: throw IllegalArgumentException("no uri found")
            val videoFile = DoorUri.parse(uri).toFile()
            var newVideo = File(videoFile.parentFile, "new${videoFile.nameWithoutExtension}.mp4")

            val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

            Napier.d(tag = VIDEO_JVM, message = "conversion Params compress video is $compressVideo")

            if(compressVideo) {
                val fileVideoDimensionsAndAspectRatio = ShrinkUtils.getVideoResolutionMetadata(videoFile)
                val newVideoDimensions = Pair(fileVideoDimensionsAndAspectRatio.first, fileVideoDimensionsAndAspectRatio.second).fitWithin()
                ShrinkUtils.optimiseVideo(videoFile, newVideo, newVideoDimensions, fileVideoDimensionsAndAspectRatio.third)
            }else{
                newVideo = videoFile
            }

            val container = Container().apply {
                containerContentEntryUid = jobItem.cjiContentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = newVideo.length()
                this.mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insert(this)
            }

            val containerFolder = jobItem.toUri ?: defaultContainerDir.toURI().toString()
            val containerFolderUri = DoorUri.parse(containerFolder)

            repo.addFileToContainer(container.containerUid, newVideo.toDoorUri(), newVideo.name,
                    ContainerAddOptions(containerFolderUri))

            container
        }


        return ProcessResult(200)
    }

    suspend fun getEntry(uri: DoorUri, process: ProcessContext): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default){
            val file = uri.toFile()

            if(!supportedFileExtensions.any { file.name.endsWith(it, true) }) {
                return@withContext null
            }

            val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(file)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            ContentEntryWithLanguage().apply {
                this.title = file.nameWithoutExtension
                this.leaf = true
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
        }
    }

}