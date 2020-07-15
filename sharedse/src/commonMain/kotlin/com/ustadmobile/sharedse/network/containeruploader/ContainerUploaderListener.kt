package com.ustadmobile.sharedse.network.containeruploader

interface ContainerUploaderListener {

    fun onStart(request: ContainerUploaderRequest)

    fun onProgress(request: ContainerUploaderRequest, bytesUploaded: Long,
                   contentLength: Long)

    fun onDone(request: ContainerUploaderRequest, status: Int)

}