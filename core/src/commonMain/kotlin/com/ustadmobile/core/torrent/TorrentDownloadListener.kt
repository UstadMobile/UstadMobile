package com.ustadmobile.core.torrent

interface TorrentDownloadListener {

    fun onComplete()

    fun onProgress(progress: Int)
}