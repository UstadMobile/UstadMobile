package com.ustadmobile.core.networkmanager.downloadmanager

interface ContainerDownloadRunner {

    suspend fun startDownload()

    suspend fun cancel()

    suspend fun pause()

    var meteredDataAllowed: Boolean

}