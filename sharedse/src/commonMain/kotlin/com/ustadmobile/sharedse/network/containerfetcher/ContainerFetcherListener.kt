package com.ustadmobile.sharedse.network.containerfetcher

interface ContainerFetcherListener {

    fun onStart(request: ContainerFetcherRequest)

    fun onProgress(request: ContainerFetcherRequest, bytesDownloaded: Long,
                   contentLength: Long)

    fun onDone(request: ContainerFetcherRequest, status: Int)

}