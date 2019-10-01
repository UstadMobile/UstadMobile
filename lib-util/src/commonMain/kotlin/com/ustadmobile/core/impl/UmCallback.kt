package com.ustadmobile.core.impl

import kotlin.js.JsName

/**
 * Generic callback wrapper
 */

interface UmCallback<T> {

    /**
     * Called when the operation completes successfully
     *
     * @param result result of the operation
     */
    @JsName("onSuccess")
    fun onSuccess(result: T?)

    /**
     * Called when the operation has failed
     *
     * @param exception exception thrown (if any)
     */
    @JsName("onFailure")
    fun onFailure(exception: Throwable?)
}
