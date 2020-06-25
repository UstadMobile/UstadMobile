package com.ustadmobile.core.view

/**
 * created @author kileha3
 */
interface ContainerMounter {

    suspend fun mountContainer(containerUid: Long): String

    suspend fun unMountContainer(mountPath: String)
}