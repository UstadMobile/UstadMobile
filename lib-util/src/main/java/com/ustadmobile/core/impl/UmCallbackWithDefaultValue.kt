package com.ustadmobile.core.impl

class UmCallbackWithDefaultValue<T>(private val defaultVal: T, callback: UmCallback<*>) : UmCallback<T> {

    private val callback: UmCallback<T> = callback as UmCallback<T>

    override fun onSuccess(result: T) {
        if (result != null)
            UmCallbackUtil.onSuccessIfNotNull(callback, result)
        else
            UmCallbackUtil.onSuccessIfNotNull(callback, defaultVal)
    }

    override fun onFailure(exception: Throwable) {
        UmCallbackUtil.onFailIfNotNull(callback, exception)
    }
}
