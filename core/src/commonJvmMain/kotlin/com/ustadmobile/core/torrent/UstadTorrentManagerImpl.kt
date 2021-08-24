package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.*
import com.turn.ttorrent.client.storage.FairPieceStorageFactory
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.io.ext.UstadFileCollectionStorage.createFileCollectionStorage
import com.ustadmobile.core.util.DiTag
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.File
import java.net.InetAddress


class UstadTorrentManagerImpl(val endpoint: Endpoint, override val di: DI) : UstadTorrentManager {

    private val torrentDir = di.direct.instance<File>(tag = DiTag.TAG_TORRENT_DIR)

    private val containerDir = di.direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private var containerInfoHashMap = mutableMapOf<Long, String>()

    private val communicationManager = di.direct.instance<UstadCommunicationManager>()

    val pieceStorage: FairPieceStorageFactory by lazy {
        FairPieceStorageFactory.INSTANCE
    }

    override suspend fun start() {
        torrentDir.listFiles { file: File ->
            file.name.endsWith(".torrent")
        }?.forEach { torrentFile ->
            addTorrent(torrentFile.nameWithoutExtension.toLong())
        }

        communicationManager.start(InetAddress.getByName("0.0.0.0"))
    }

    override suspend fun addTorrent(containerUid: Long) {
        withContext(Dispatchers.Default){

            val torrentFile = File(torrentDir, "$containerUid.torrent")

            val containerFilesDir = File(containerDir, containerUid.toString())
            containerFilesDir.mkdirs()

            if(!torrentFile.exists())
                throw IllegalArgumentException("torrent file does not exist ${torrentFile.absolutePath}")


            // create the torrent
            val metadataProvider = FileMetadataProvider(torrentFile.absolutePath)
            val fileCollectionStorage = createFileCollectionStorage(metadataProvider.torrentMetadata, containerFilesDir)
            val pieceStorage = pieceStorage.createStorage(metadataProvider.torrentMetadata, fileCollectionStorage)

            // add to map for ref
            containerInfoHashMap[containerUid] = metadataProvider.torrentMetadata.hexInfoHash
            val manager = communicationManager.addTorrent(metadataProvider, pieceStorage)

            val torrentDeferred = CompletableDeferred<TorrentManager>(launch(Dispatchers.Default){
                while(true){
                    delay(100)
                    if(!isActive){
                        torrentDeferred?.completeExceptionally(CancellationException())
                    }
                }
            })

            manager.addListener(object: TorrentListener{
                override fun peerConnected(peerInformation: PeerInformation?) {

                }

                override fun peerDisconnected(peerInformation: PeerInformation?) {
                }

                override fun pieceDownloaded(pieceInformation: PieceInformation?, peerInformation: PeerInformation?) {
                }

                override fun downloadComplete() {
                    torrentDeferred?.complete(manager)
                }

                override fun pieceReceived(pieceInformation: PieceInformation?, peerInformation: PeerInformation?) {
                }

                override fun downloadFailed(cause: Throwable?) {
                    if (cause != null) {
                        torrentDeferred?.completeExceptionally(cause)
                    }
                }

                override fun validationComplete(validpieces: Int, totalpieces: Int) {

                }

            })

        }
    }

    override suspend fun removeTorrent(containerUid: Long) {
        val hexInfoHash = containerInfoHashMap.remove(containerUid) ?: return
        communicationManager.removeTorrent(hexInfoHash)
    }

    override suspend fun stop() {
        communicationManager.stop()
    }

}