package com.ustadmobile.core.networkmanager

abstract class ContainerUploadManager {

    abstract suspend fun enqueue(uploadJobId: Long)

}