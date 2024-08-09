package com.ustadmobile.core.domain.interop

/**
 * An exception that can be thrown by logic which implements an HTTP API.
 * @param statusCode the HTTP Status Code to use for the response e.g. 400
 */
class HttpApiException(
    val statusCode: Int, message: String?, cause: Throwable? = null
): Exception(message, cause)
