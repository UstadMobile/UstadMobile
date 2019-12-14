package com.ustadmobile.core.networkmanager.downloadmanager

interface ContainerDownloadRunner {

    fun cancel()

    fun pause()

    var meteredDataAllowed: Boolean

}