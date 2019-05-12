package com.ustadmobile.core.impl.http

import java.io.IOException

/**
 * Created by mike on 12/26/17.
 */

class UmHttpException : IOException {

    private var response: UmHttpResponse? = null

    var rootCause: Exception? = null

    val status: Int
        get() = response?.status ?: -1

    constructor(response: UmHttpResponse) {
        this.response = response
    }

    constructor(rootCause: Exception) {
        this.rootCause = rootCause
    }

}
