package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.*
import com.turn.ttorrent.client.storage.FairPieceStorageFactory
import com.turn.ttorrent.tracker.TrackedTorrent
import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.io.ext.UstadFileCollectionStorage.createFileCollectionStorage
import com.ustadmobile.core.util.DiTag
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File


class UstadTorrentManagerImpl(val endpoint: Endpoint, override val di: DI) : UstadTorrentManager {

    private val torrentDir = di.direct.instance<File>(tag = DiTag.TAG_TORRENT_DIR)

    private val containerDir = di.direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private var containerInfoHashMap = mutableMapOf<Long, String>()

    private var containerManagerMap = mutableMapOf<Long, TorrentManager>()

    private var containerListenerMap = mutableMapOf<Long, DownloadListenerAdapter>()

    private val communicationManager = di.direct.instance<UstadCommunicationManager>()

    // lock add and removeTorrent
    private val torrentLock = Mutex()

    val pieceStorage: FairPieceStorageFactory by lazy {
        FairPieceStorageFactory.INSTANCE
    }

    override suspend fun start() {
        torrentDir.listFiles { file: File ->
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
            val fileCollectionStorage = createFileCollectionStorage(metadataProvider.torrentMetadata, containerFilesDir)
            val pieceStorage = pieceStorage.createStorage(metadataProvider.torrentMetadata, fileCollectionStorage)

            // add to map for ref
            containerInfoHashMap[containerUid] = metadataProvider.torrentMetadata.hexInfoHash
            println("prepared to add torrent in ${System.currentTimeMillis() - startTime} ms")
            val manager = communicationManager.addTorrent(metadataProvider, pieceStorage)
            containerManagerMap[containerUid] = manager
        }
    }

    override fun addDownloadListener(containerUid: Long, downloadListener: TorrentDownloadListener) {
        val manager = containerManagerMap[containerUid]
        val adapter = DownloadListenerAdapter(downloadListener)
        containerListenerMap[containerUid] = adapter
        manager?.addListener(adapter)
    }

    override fun removeDownloadListener(containerUid: Long, downloadListener: TorrentDownloadListener) {
        val manager = containerManagerMap.remove(containerUid)
        val adapter = containerListenerMap.remove(containerUid)
        manager?.removeListener(adapter)
    }

    override suspend fun removeTorrent(containerUid: Long) {
        val hexInfoHash = containerInfoHashMap.remove(containerUid) ?: return
        communicationManager.removeTorrent(hexInfoHash)
    }

    override suspend fun stop() {
        containerInfoHashMap.keys.toList().forEach {
            torrentLock.withLock {
                removeTorrent(it)
            }
        }
    }

}