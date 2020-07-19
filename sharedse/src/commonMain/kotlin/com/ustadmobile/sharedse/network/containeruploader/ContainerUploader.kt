package com.ustadmobile.sharedse.network.containeruploader

import kotlinx.coroutines.Deferred

data class ContainerUploaderRequest(val uploadJobUid: Long, val fileList: String, val uploadToUrl: String, val endpointUrl: String)

abstract class ContainerUploader() {

    abstract suspend fun enqueue(request: ContainerUploaderRequest,
                                 listener: ContainerUploaderListener? = null): Deferred<Int>

}