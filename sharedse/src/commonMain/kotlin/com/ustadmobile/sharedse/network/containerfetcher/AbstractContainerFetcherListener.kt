package com.ustadmobile.sharedse.network.containerfetcher

abstract class AbstractContainerFetcherListener: ContainerFetcherListener {

    override fun onStart(request: ContainerFetcherRequest) {

    }

    override fun onProgress(request: ContainerFetcherRequest, bytesDownloaded: Long, contentLength: Long) {

    }

    override fun onDone(request: ContainerFetcherRequest, status: Int) {

    }
}