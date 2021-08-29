package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.PeerInformation
import com.turn.ttorrent.client.PieceInformation
import com.turn.ttorrent.client.TorrentListener

class DownloadListenerAdapter(val destListener: TorrentDownloadListener) : TorrentListener {

    override fun peerConnected(peerInformation: PeerInformation?) {

    }

    override fun peerDisconnected(peerInformation: PeerInformation?) {

    }

    override fun pieceDownloaded(pieceInformation: PieceInformation?, peerInformation: PeerInformation?) {

    }

    override fun downloadComplete() {
        destListener.onComplete()
    }

    override fun pieceReceived(pieceInformation: PieceInformation?, peerInformation: PeerInformation?) {

    }

    override fun downloadFailed(cause: Throwable?) {

    }

    override fun validationComplete(validpieces: Int, totalpieces: Int) {

    }
}