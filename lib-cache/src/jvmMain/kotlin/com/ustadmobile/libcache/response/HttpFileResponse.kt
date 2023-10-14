package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeader
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.HttpHeadersImpl
import com.ustadmobile.libcache.request.HttpRequest
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class HttpFileResponse(
    private val file: File,
    override val request: HttpRequest,
    val extraHeaders: List<HttpHeader> = emptyList(),
): HttpResponseJvm {

    override val headers: HttpHeaders
        get() = HttpHeadersImpl(extraHeaders)

    init {
        //Generate headers based on the file itself
    }

    override fun bodyInputStream(): InputStream {
        return FileInputStream(file)
    }


}