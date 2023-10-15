package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheJvm
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.request.requestBuilder
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HttpFileResponse(
    private val file: File,
    mimeType: String,
    override val request: HttpRequest,
    extraHeaders: HttpHeaders? = null,
): HttpResponseJvm {

    override val headers: HttpHeaders

    init {
        headers = headersBuilder {
            header("Content-Length", file.length().toString())
            header("Content-Type", mimeType)
            header("Last-Modified", LAST_MODIFIED_FORMATTER.format(
                Date(file.lastModified())
            ))
            extraHeaders?.also { takeFrom(it) }
        }
    }

    constructor(
        cache: UstadCache,
        file: File,
        url: String
    ) : this(
        file = file,
        mimeType = (cache as UstadCacheJvm).mimeTypeHelper.mimeTypeByUri(file.toURI().toString())
            ?: "application/octet-stream",
        request = requestBuilder {
            this.url = url
        }
    )


    override fun bodyInputStream(): InputStream {
        return FileInputStream(file)
    }

    companion object {

        /**
         * As per
         * https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
         */
        val LAST_MODIFIED_FORMATTER = SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US
        ).also {
            it.timeZone = TimeZone.getTimeZone("UTC")
        }

    }


}