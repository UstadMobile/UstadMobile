package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.impl.http.UmHttpResponse

import java.io.IOException
import java.io.InputStream

import okhttp3.Response

/**
 * Simple wrapper to wrap the OK HTTP library response object
 */

class UmHttpResponseSe(private val response: Response) : UmHttpResponse() {

    override val responseBody: ByteArray?
        get() {
            try {
                return response.body()!!.bytes()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

    override val responseAsStream: InputStream?
        get() = response.body()!!.byteStream()

    override val isSuccessful: Boolean
        get() = response.isSuccessful

    override val status: Int
        get() = response.code()

    override fun getHeader(headerName: String): String? {
        return response.header(headerName)
    }
}
