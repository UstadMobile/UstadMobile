package com.ustadmobile.core.torrent

import bt.Bt
import bt.runtime.BtClient
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import bt.runtime.BtRuntime
import bt.torrent.maker.TorrentBuilder
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.nio.file.Paths
import java.util.*


class SeedManagerImpl(val endpoint: Endpoint, override val di: DI) : SeedManager {

    private val torrentDir = di.direct.instance<File>(tag = DiTag.TAG_TORRENT_DIR)

    private val containerDir = di.direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val storage = SharedFileSystemStorage(containerDir)

    var sharedRuntime: BtRuntime = BtRuntime.defaultRuntime()

    private var containerClientMap = mutableMapOf<Long, BtClient>()

    override suspend fun start() {
        sharedRuntime.startup()

        torrentDir.listFiles { file: File ->
            file.name.endsWith(".torrent")
        }?.forEach { torrentFile ->
            addTorrent(torrentFile.nameWithoutExtension.toLong())
        }
    }

    override suspend fun addTorrent(containerUid: Long) {
        withContext(Dispatchers.Default){

            val torrentFile = File(torrentDir, "$containerUid.torrent")

            if(!torrentFile.exists())
                throw IllegalArgumentException("torrent file does not exist ${torrentFile.absolutePath}")

            val existingClient = containerClientMap[containerUid]
            if(existingClient != null){
                // client already exists
                val hasClient = sharedRuntime.clients.contains(existingClient)
                // remove the client from sharedRunTime
                if(hasClient){
                    existingClient.stop()
                    sharedRuntime.detachClient(existingClient)
                }
            }

            // create the torrent
            val btClient = Bt
                    .client(sharedRuntime)
                    .storage(storage)
                    .torrent(torrentFile.toURI().toURL())
                    .build()

            // add to map for ref
            containerClientMap[containerUid] = btClient

            sharedRuntime.attachClient(btClient)

            btClient.startAsync()
        }
    }

    override suspend fun removeTorrent(containerUid: Long) {
        val btClient = containerClientMap.remove(containerUid) ?: return
        btClient.stop()
        sharedRuntime.detachClient(btClient)
    }

    override suspend fun stop() {
        sharedRuntime.shutdown()
    }


}