package com.ustadmobile.core.network.containerfetcher

interface ContainerFetcherListener2 {

    fun onStart(request: ContainerFetcherRequest2)

    fun onProgress(request: ContainerFetcherRequest2, bytesDownloaded: Long,
                   contentLength: Long)

    fun onDone(request: ContainerFetcherRequest2, status: Int)

}