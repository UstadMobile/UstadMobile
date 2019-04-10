package com.ustadmobile.core.impl

/**
 * Generic callback wrapper
 */

interface UmCallback<T> {

    /**
     * Called when the operation completes successfully
     *
     * @param result result of the operation
     */
    fun onSuccess(result: T?)

    /**
     * Called when the operation has failed
     *
     * @param exception exception thrown (if any)
     */
    fun onFailure(exception: Throwable)
}
