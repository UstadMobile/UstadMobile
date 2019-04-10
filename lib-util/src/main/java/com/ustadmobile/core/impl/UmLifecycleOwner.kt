package com.ustadmobile.core.impl

/**
 * Represents an object (e.g. presenter) that has a lifecycle.
 */
interface UmLifecycleOwner {

    /**
     * Get the system context object
     *
     * @return the system context object
     */
    val context: Any

    /**
     * Add an event listener for lifecycle changes
     *
     * @param listener event listener
     */
    fun addLifecycleListener(listener: UmLifecycleListener)

    /**
     * Remove an event listener for lifecycle changes
     *
     * @param listener event listener
     */
    fun removeLifecycleListener(listener: UmLifecycleListener)

    companion object {

        const val STATUS_CREATED = 1

        const val STATUS_STARTED = 2

        const val STATUS_RESUMED = 3

        const val STATUS_PAUSED = 4

        const val STATUS_STOPPED = 5

        const val STATUS_DESTROYED = 6
    }

}
