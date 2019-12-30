package com.ustadmobile.sharedse.network.containerfetcher

import kotlinx.coroutines.Deferred

data class ContainerFetcherRequest(val url: String, val fileDest: String) {
    val id: Int
        get() = (url.hashCode() * 31) + fileDest.hashCode()
}

abstract class ContainerFetcher() {

    abstract suspend fun enqueue(request: ContainerFetcherRequest,
                                 listener: ContainerFetcherListener? = null): Deferred<Int>

}