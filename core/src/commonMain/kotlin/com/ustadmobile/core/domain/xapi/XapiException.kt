package com.ustadmobile.core.domain.xapi

class XapiException(
    val responseCode: Int,
    message: String,
    cause: Throwable? = null
): Exception(message, cause)
