package com.ustadmobile.core.impl;

/**
 * Represents an object (e.g. presenter) that has a lifecycle.
 */
public interface UmLifecycleOwner {

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

}
