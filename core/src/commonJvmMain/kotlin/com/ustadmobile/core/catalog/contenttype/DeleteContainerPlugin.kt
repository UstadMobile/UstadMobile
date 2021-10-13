package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class DeleteContainerPlugin(private var context: Any, private val endpoint: Endpoint, override val di: DI): ContentPlugin {

    private val httpClient: HttpClient = di.direct.instance()

    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val containerDir = di.direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val torrentDir = di.direct.instance<File>(tag = DiTag.TAG_TORRENT_DIR)

    private val ustadTorrentManager: UstadTorrentManager = di.direct.instance<UstadTorrentManager>()


    override val pluginId: Int
        get() = PLUGIN_ID
    override val supportedMimeTypes: List<String>
        get() = TODO("Not yet implemented")
    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        val containerUid = uri.uri.toString().substringAfterLast("/").toLongOrNull() ?: return null

        val containerWithFiles = db.containerEntryDao.findByContainer(containerUid)

        if(containerWithFiles.isEmpty()){
            return null
        }

        val container = repo.containerDao.findByUid(containerUid) ?: return null

        val contentEntry = repo.contentEntryDao.findByUid(container.containerUid)
                ?: throw IllegalArgumentException("no entry found from container")

        return MetadataResult(contentEntry as ContentEntryWithLanguage, PLUGIN_ID)
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {

        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")

        // delete all containerEntries for this contentEntry
        db.containerEntryDao.deleteByContentEntryUid(contentJobItem.cjiContentEntryUid)

        var numFailures = 0
        db.runInTransaction {
            var zombieEntryFilesList: List<ContainerEntryFile>
            do {
                zombieEntryFilesList = db.containerEntryFileDao.findZombieEntries()
                zombieEntryFilesList.forEach {
                    val filePath = it.cefPath
                    if (filePath == null || !File(filePath).delete()) {
                        numFailures++
                    }
                }

                db.containerEntryFileDao.deleteListOfEntryFiles(zombieEntryFilesList)
            } while (zombieEntryFilesList.isNotEmpty())
        }

        val containers = db.containerDao.findFilesByContentEntryUid(contentJobItem.cjiContentEntryUid)

        containers.forEach {
            val torrentFile = File(torrentDir, "${it.containerUid}.torrent")
            if(torrentFile.exists()){
                torrentFile.delete()
            }
        }

        return if(numFailures == 0){
            ProcessResult(JobStatus.DELETED)
        }else{
            ProcessResult(JobStatus.FAILED)
        }
    }

    companion object {

        const val PLUGIN_ID = 14
    }
}