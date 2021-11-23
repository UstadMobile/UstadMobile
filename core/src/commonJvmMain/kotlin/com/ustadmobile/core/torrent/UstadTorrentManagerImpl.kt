package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.*
import com.turn.ttorrent.client.storage.FairPieceStorageFactory
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.UstadFileCollectionStorage.createFileCollectionStorage
import com.ustadmobile.core.util.DiTag
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.on
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File


interface CommunicationManagerListener {

    fun onCommunicationManagerChanged(
            meteredNetwork: Boolean
    )
}

class UstadTorrentManagerImpl(
        val endpoint: Endpoint,
        override val di: DI
) : UstadTorrentManager, CommunicationManagerListener {

    private val torrentDir: File by di.on(endpoint).instance(tag = DiTag.TAG_TORRENT_DIR)

    private val containerDir by di.on(endpoint).instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_DB)

    private var containerInfoHashMap = mutableMapOf<Long, String>()

    private var activeTorrentDownloadMap = mutableMapOf<Long, ActiveTorrentInfo>()

    // on android, di uses connectionManager and a provider to get the current communicationManager
    // on jvm, communicationManager is a singleton
    private val communicationManager: UstadCommunicationManager?
        get() = di.direct.instanceOrNull()

    // lock add and removeTorrent
    private val torrentLock = Mutex()

    private val pieceStorage: FairPieceStorageFactory by lazy {
        FairPieceStorageFactory.INSTANCE
    }

    private fun requireCommunicationManager(): UstadCommunicationManager{
        return communicationManager ?: throw IllegalStateException("no communicationManager found")
    }

    override suspend fun startSeeding() {
        torrentDir.listFiles() { file: File ->
            file.name.endsWith(".torrent")
        }?.forEach { torrentFile ->
            torrentLock.withLock {
                    addTorrent(torrentFile.nameWithoutExtension.toLong(), null)
            }
        }
    }

    override suspend fun addTorrent(containerUid: Long, downloadPath: String?) {
        withContext(Dispatchers.Default){

            val startTime = System.currentTimeMillis()

            val torrentFile = File(torrentDir, "$containerUid.torrent")

            val containerFilesDir = if(downloadPath != null) File(downloadPath)
                                    else File(containerDir, containerUid.toString())
            containerFilesDir.mkdirs()
            if(!torrentFile.exists())
                throw IllegalArgumentException("torrent file does not exist ${torrentFile.absolutePath}")

            // create the torrent
            val metadataProvider = FileMetadataProvider(torrentFile.absolutePath)
            val fileCollectionStorage = createFileCollectionStorage(
                    metadataProvider.torrentMetadata,
                    containerFilesDir,
                    db)

            val pieceStorage = pieceStorage.createStorage(metadataProvider.torrentMetadata, fileCollectionStorage)

            // add to map for ref
            containerInfoHashMap[containerUid] = metadataProvider.torrentMetadata.hexInfoHash
            println("prepared to add torrent in ${System.currentTimeMillis() - startTime} ms")
            val manager = requireCommunicationManager().addTorrent(metadataProvider, pieceStorage)

            val checkActiveTorrent = activeTorrentDownloadMap[containerUid]
            activeTorrentDownloadMap[containerUid] = ActiveTorrentInfo(containerFilesDir.path, manager, checkActiveTorrent?.downloadListener)
        }
    }

    override fun addDownloadListener(containerUid: Long, downloadListener: TorrentDownloadListener) {
        val activeTorrent = activeTorrentDownloadMap[containerUid] ?: return
        val adapter = DownloadListenerAdapter(downloadListener)
        activeTorrent.downloadListener = adapter
        activeTorrent.torrentManager.addListener(adapter)
        activeTorrentDownloadMap[containerUid] = activeTorrent
    }

    override fun removeDownloadListener(containerUid: Long) {
        val activeTorrent = activeTorrentDownloadMap.remove(containerUid)
        activeTorrent?.torrentManager?.removeListener(activeTorrent.downloadListener)
    }

    override suspend fun removeTorrent(containerUid: Long) {
        val hexInfoHash = containerInfoHashMap.remove(containerUid) ?: return
        removeDownloadListener(containerUid)
        communicationManager?.removeTorrent(hexInfoHash)
    }

    override fun onCommunicationManagerChanged(meteredNetwork: Boolean) {
        GlobalScope.launch(Dispatchers.Default){

            // go through map for active downloads
            activeTorrentDownloadMap.forEach {
                // need to re-add the torrents to the new communicationManager
                torrentLock.withLock{
                    addTorrent(it.key, it.value.downloadPath)
                    it.value.torrentManager.addListener(it.value.downloadListener)
                }
            }
            // start seeding
            if(!meteredNetwork){
                startSeeding()
            }


        }
    }

}