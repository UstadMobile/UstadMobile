package com.ustadmobile.core.view

/**
 * created @author kileha3
 */
interface MountedContainerHandler {

    suspend fun mountContainer(containerUid: Long): String

    suspend fun unMountContainer()
}