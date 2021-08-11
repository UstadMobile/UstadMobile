package com.ustadmobile.core.torrent

import org.kodein.di.DIAware

interface SeedManager : DIAware {

    suspend fun start()

    suspend fun addTorrent(containerUid: Long)

    suspend fun removeTorrent(containerUid: Long)

    suspend fun stop()

}