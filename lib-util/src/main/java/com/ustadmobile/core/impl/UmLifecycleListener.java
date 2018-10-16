package com.ustadmobile.core.impl;

/**
 * An event listener that will receive an event each time a lifecycle event occurs.
 */
public interface UmLifecycleListener {

    /**
     * Object created
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    void onLifecycleCreate(UmLifecycleOwner lifecycleOwner);

    /**
     * Object started (e.g. about to become visible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    void onLifecycleStart(UmLifecycleOwner lifecycleOwner);

    /**
     * Object resumed (visible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    void onLifecycleResume(UmLifecycleOwner lifecycleOwner);

    /**
     * Object paused (becoming invisible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    void onLifecyclePause(UmLifecycleOwner lifecycleOwner);

    /**
     * Object stopped (invisible)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    void onLifecycleStop(UmLifecycleOwner lifecycleOwner);

    /**
     * Object destroyed (invisible, end of life)
     *
     * @param lifecycleOwner Lifecycle owner object
     */
    void onLifecycleDestroy(UmLifecycleOwner lifecycleOwner);
}
