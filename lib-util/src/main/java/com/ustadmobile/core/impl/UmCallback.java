package com.ustadmobile.core.impl;

/**
 * Generic callback wrapper
 */

public interface UmCallback<T> {

    /**
     * Called when the operation completes successfully
     *
     * @param result result of the operation
     */
    void onSuccess(T result);

    /**
     * Called when the operation has failed
     *
     * @param exception exception thrown (if any)
     */
    void onFailure(Throwable exception);

}
