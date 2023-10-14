package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.db.entities.ResponseBody
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.request.HttpRequest
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

class CacheResponseJvm(
    override val request: HttpRequest,
    override val headers: HttpHeaders,
    private val responseBody: ResponseBody,
) : HttpResponseJvm {

    override fun bodyInputStream(): InputStream {
        val file = File(URI(responseBody.storageUri).path)
        return FileInputStream(file)
    }
}