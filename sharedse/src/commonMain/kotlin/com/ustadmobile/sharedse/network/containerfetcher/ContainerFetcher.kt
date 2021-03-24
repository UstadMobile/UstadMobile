package com.ustadmobile.sharedse.network.containerfetcher

import com.ustadmobile.core.network.containerfetcher.ContainerFetcherListener2
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherRequest2
import kotlinx.coroutines.Deferred

abstract class ContainerFetcher() {

    abstract suspend fun enqueue(request: ContainerFetcherRequest2,
                                 listener: ContainerFetcherListener2? = null): Deferred<Int>

}