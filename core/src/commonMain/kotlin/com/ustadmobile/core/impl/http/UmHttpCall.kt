package com.ustadmobile.core.impl.http

/**
 * A wrapper that represents an HTTP call
 */
abstract class UmHttpCall {

    /**
     * Cancel the call if it is currently in progress (e.g. if the user has left the view)
     */
    abstract fun cancel()

}
