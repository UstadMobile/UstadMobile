package com.ustadmobile.core.networkmanager.downloadmanager

import kotlinx.coroutines.Deferred

interface ContainerDownloadRunner {

    suspend fun download(): Deferred<Int>

    suspend fun cancel()

    suspend fun pause()

    var meteredDataAllowed: Boolean

}