package com.ustadmobile.core.torrent

import org.kodein.di.DIAware

/*
 * UstadTorrentManager manage torrent seeding and downloading.
 * It is endpoint (context) specific.
 * It will communicate with and manage the underlying torrent servers on the platform.
 */
interface UstadTorrentManager : DIAware {

    /**
     * loops torrentDir and start seeding all available torrent files
     */
    suspend fun startSeeding()

    /**
     * open a torrentFile and adds it to communicationManager to start downloading/seeding
     */
    suspend fun addTorrent(containerUid: Long, downloadPath: String?)

    /**
     * adds a download listener to an active torrent
     */
    fun addDownloadListener(containerUid: Long,downloadListener: TorrentDownloadListener)

    /**
     * removes a download listener from an active torrent
     */
    fun removeDownloadListener(containerUid: Long)

    /**
     * removes a torrent from being downloading/seeding
     */
    suspend fun removeTorrent(containerUid: Long)

}