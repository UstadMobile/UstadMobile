package com.ustadmobile.core.networkmanager

import kotlinx.coroutines.Deferred

data class ContainerUploaderRequest(val uploadJobUid: Long, val fileList: String, val uploadToUrl: String, val endpointUrl: String)

abstract class ContainerUploaderCommon() {

    abstract suspend fun enqueue(request: ContainerUploaderRequest): Deferred<Int>

}