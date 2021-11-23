package com.ustadmobile.core.torrent

interface TorrentDownloadListener {

    fun onComplete()

    /**
     * Represents the progress of the torrent download 0 - total file size
     */
    fun onProgress(progress: Int)

    fun onDownloadFailed(cause: Throwable?)
}