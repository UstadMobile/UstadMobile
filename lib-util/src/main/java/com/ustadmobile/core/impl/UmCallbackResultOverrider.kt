package com.ustadmobile.core.impl

/**
 * Used to override the result of a callback (e.g. when doing an insert, and syncable primary keys
 * have already been generated.
 *
 * @param <T> Callback result type
</T> */
class UmCallbackResultOverrider<T>(private val callback: UmCallback<T>?, private val resultOverride: T) : UmCallback<T> {

    override fun onSuccess(result: T?) {
        callback?.onSuccess(resultOverride)
    }

    override fun onFailure(exception: Throwable) {
        callback?.onFailure(exception)
    }
}
