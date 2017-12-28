package com.ustadmobile.core.impl;

/**
 * Generic callback wrapper
 */

public interface UmCallback {

    /**
     * Called when the operation completes successfully
     *
     * @param result result of the operation
     */
    void onSuccess(Object result);

    /**
     * Called when the operation has failed
     *
     * @param exception exception thrown (if any)
     */
    void onFailure(Throwable exception);

}
