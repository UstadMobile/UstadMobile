package com.ustadmobile.core.impl;

/**
 * Represents an object (e.g. presenter) that has a lifecycle.
 */
public interface UmLifecycleOwner {

    int STATUS_CREATED = 1;

    int STATUS_STARTED = 2;

    int STATUS_RESUMED = 3;

    int STATUS_PAUSED = 4;

    int STATUS_STOPPED = 5;

    int STATUS_DESTROYED = 6;

    /**
     * Get the system context object
     *
     * @return the system context object
     */
    Object getContext();

    /**
     * Add an event listener for lifecycle changes
     *
     * @param listener event listener
     */
    void addLifecycleListener(UmLifecycleListener listener);

    /**
     * Remove an event listener for lifecycle changes
     *
     * @param listener event listener
     */
    void removeLifecycleListener(UmLifecycleListener listener);

    int getCurrentStatus();

}
