package com.ustadmobile.core.impl;

/**
 * Generic callback wrapper
 */

public interface UmCallback {

    /**
     * Called when the operation completes successfully
     *
     * @param requestId requestId as supplied when the operation was requested
     * @param result result of the operation
     */
    void onSuccess(int requestId, Object result);

    /**
     * Called when the operation has failed
     *
     * @param requestId requestId as supplied when the operation was requested
     * @param reason reason for failure (if any)
     * @param exception exception thrown (if any)
     */
    void onFailure(int requestId, Object reason, Throwable exception);

}
