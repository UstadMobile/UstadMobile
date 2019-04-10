package com.ustadmobile.core.impl

object UmCallbackUtil {

    fun <T> onSuccessIfNotNull(callback: UmCallback<T>?, result: T) {
        callback?.onSuccess(result)
    }

    fun <T> onFailIfNotNull(callback: UmCallback<T>?, exception: Throwable) {
        callback?.onFailure(exception)
    }
}
