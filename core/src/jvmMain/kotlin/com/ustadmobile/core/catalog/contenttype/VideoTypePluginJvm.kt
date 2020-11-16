package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoTypePluginJvm: VideoTypePlugin() {

    override suspend fun extractMetadata(filePath: String): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default){
            val file = File(filePath)

            if(!fileExtensions.any { file.name.endsWith(it, true) }) {
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

    override suspend fun importToContainer(filePath: String, conversionParams: Map<String, String>,
                                           contentEntryUid: Long, mimeType: String, containerBaseDir: String,
                                           context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        return withContext(Dispatchers.Default) {

            val videoFile = File(filePath.removePrefix("file://"))
            val newVideo = File(videoFile.parentFile, "new${videoFile.nameWithoutExtension}.mp4")

            val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(videoFile)
            val newVideoDimensions = fileVideoDimensions.fitWithin()

            ShrinkUtils.optimiseVideo(videoFile, newVideo, newVideoDimensions)

            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = newVideo.length()
                this.mimeType = mimeType
                containerUid = repo.containerDao.insert(this)
            }

            val containerManager = ContainerManager(container, db, repo, containerBaseDir)

            containerManager.addEntries(ContainerManager.FileEntrySource(newVideo, newVideo.name))

            container
        }
    }
}