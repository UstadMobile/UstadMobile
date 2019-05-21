package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.impl.http.UmHttpCall

import okhttp3.Call

/**
 * Simple wrapper to map to OK HTTP library
 */

class UmHttpCallSe
/**
 *
 * @param call the OK Http Call object
 */
(private val call: Call) : UmHttpCall() {

    override fun cancel() {
        call.cancel()
    }
}
