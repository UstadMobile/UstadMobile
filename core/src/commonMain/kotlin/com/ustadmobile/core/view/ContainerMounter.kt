package com.ustadmobile.core.view

/**
 * created @author kileha3
 */
interface ContainerMounter {

    suspend fun mountContainer(endpointUrl: String, containerUid: Long, filterMode: Int = FILTER_MODE_NONE): String

    suspend fun unMountContainer(endpointUrl: String, mountPath: String)

    companion object {
        /**
         * Do not filter any responses
         */
        const val FILTER_MODE_NONE = 0

        /**
         * Filter HTML mime types. A meta viewport tag and some basic responsive css will be
         * automatically added to any HTML that does not already have it.
         */
        const val FILTER_MODE_EPUB = 1
    }
}