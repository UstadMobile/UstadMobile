package com.ustadmobile.core.view

/**
 * created @author kileha3
 */
interface ContainerMounter {

    suspend fun mountContainer(endpointUrl: String, containerUid: Long): String

    suspend fun unMountContainer(endpointUrl: String, mountPath: String)
}