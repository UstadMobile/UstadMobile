package com.ustadmobile.core.torrent

import org.kodein.di.DIAware

interface UstadTorrentManager : DIAware {

    suspend fun start()

    suspend fun addTorrent(containerUid: Long, downloadPath: String?)

    fun addDownloadListener(containerUid: Long,downloadListener: TorrentDownloadListener)

    fun removeDownloadListener(containerUid: Long, downloadListener: TorrentDownloadListener)

    suspend fun removeTorrent(containerUid: Long)

    suspend fun stop()

}