package com.ustadmobile.sharedse.network.containeruploader

import kotlinx.coroutines.Deferred

data class ContainerUploaderRequest(val fromFile: String, val uploadToUrl: String)

abstract class ContainerFetcher() {

    abstract suspend fun enqueue(request: ContainerUploaderRequest,
                                 listener: ContainerUploaderListener? = null): Deferred<Int>

}