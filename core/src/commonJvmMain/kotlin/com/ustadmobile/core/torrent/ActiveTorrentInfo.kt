package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.TorrentManager

data class ActiveTorrentInfo(
        val downloadPath: String,
        val torrentManager: TorrentManager,
        var downloadListener: DownloadListenerAdapter?
)