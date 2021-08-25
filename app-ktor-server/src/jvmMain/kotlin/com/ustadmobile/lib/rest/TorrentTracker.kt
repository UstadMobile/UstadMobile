package com.ustadmobile.lib.rest

import com.turn.ttorrent.tracker.TrackedTorrent
import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.DiTag
import org.kodein.di.*
import java.io.File

class TorrentTracker(val endpoint: Endpoint, override val di: DI): DIAware {

    private var tracker: Tracker = di.direct.instance()

    fun start(){
        val torrentDir = di.on(endpoint).direct.instance<File>(DiTag.TAG_TORRENT_DIR)
        torrentDir.listFiles()?.forEach {
            addTorrentFile(it)
        }
    }

    fun addTorrentFile(torrentFile: File){
        tracker.announce(TrackedTorrent.load(torrentFile))
    }

    fun addTorrentInfoHash(infoHash: ByteArray){
        tracker.announce(TrackedTorrent(infoHash))
    }

    fun stop(){
        tracker.stop()
    }

}