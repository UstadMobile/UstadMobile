package com.ustadmobile.sharedse.network.containeruploader

abstract class AbstractContainerFetcherListener: ContainerUploaderListener {

    override fun onStart(request: ContainerUploaderRequest) {

    }

    override fun onProgress(request: ContainerUploaderRequest, bytesUploaded: Long, contentLength: Long) {

    }

    override fun onDone(request: ContainerUploaderRequest, status: Int) {

    }
}