package com.ustadmobile.core.impl

/**
 * An event listener that will receive an event each time a lifecycle event occurs.
 */
interface UmLifecycleListener {

    /**
     * Object created
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    fun onLifecycleCreate(lifecycleOwner: UmLifecycleOwner)

    /**
     * Object started (e.g. about to become visible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    fun onLifecycleStart(lifecycleOwner: UmLifecycleOwner)

    /**
     * Object resumed (visible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    fun onLifecycleResume(lifecycleOwner: UmLifecycleOwner)

    /**
     * Object paused (becoming invisible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    fun onLifecyclePause(lifecycleOwner: UmLifecycleOwner)

    /**
     * Object stopped (invisible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    fun onLifecycleStop(lifecycleOwner: UmLifecycleOwner)

    /**
     * Object destroyed (invisible, end of life)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    fun onLifecycleDestroy(lifecycleOwner: UmLifecycleOwner)
}
