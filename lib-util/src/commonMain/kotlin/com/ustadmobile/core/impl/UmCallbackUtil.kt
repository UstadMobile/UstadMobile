package com.ustadmobile.core.impl

import kotlin.jvm.JvmStatic

object UmCallbackUtil {

    @JvmStatic
    fun <T> onSuccessIfNotNull(callback: UmCallback<T>?, result: T?) {
        callback?.onSuccess(result)
    }

    @JvmStatic
    fun <T> onFailIfNotNull(callback: UmCallback<T>?, exception: Throwable) {
        callback?.onFailure(exception)
    }
}
