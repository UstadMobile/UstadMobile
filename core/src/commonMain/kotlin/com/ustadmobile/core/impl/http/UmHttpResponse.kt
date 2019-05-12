package com.ustadmobile.core.impl.http

import kotlinx.io.InputStream

/**
 * A wrapper that represents an HTTP response
 */

abstract class UmHttpResponse {

    /**
     * Get the response body as a byte array. This will read the entire response into the memory if
     * it was cached elsewhere.
     *
     * @return The response body as a byte array
     *
     * @throws IOException
     */
    abstract val responseBody: ByteArray?

    /**
     *
     * @return
     * @throws IOException
     */
    abstract val responseAsStream: InputStream?


    /**
     * Returns whether or not the respnose was successful, e.g. the response has a successful http
     * response code.
     *
     * @return true if the request was successful, false otherwise (e.g. 400+ status code)
     */
    abstract val isSuccessful: Boolean

    /**
     * Returns the response status code
     *
     * @return
     */
    abstract val status: Int

    /**
     * Get a response header frmo the response
     *
     * @param headerName
     *
     * @return String with the header value
     */
    abstract fun getHeader(headerName: String): String?


}
