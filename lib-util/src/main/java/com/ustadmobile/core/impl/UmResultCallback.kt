package com.ustadmobile.core.impl

/**
 * Simple callback definition that has only one method with a single type parameter... and is thus
 * suitable for use with Lambdas
 */
interface UmResultCallback<T> {

    fun onDone(result: T)
}
