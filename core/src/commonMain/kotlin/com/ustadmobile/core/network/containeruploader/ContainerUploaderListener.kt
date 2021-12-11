package com.ustadmobile.core.network.containeruploader

interface ContainerUploaderListener {

    fun onProgress(request2: ContainerUploaderRequest2, bytesUploaded: Long, uploadSize: Long)

}