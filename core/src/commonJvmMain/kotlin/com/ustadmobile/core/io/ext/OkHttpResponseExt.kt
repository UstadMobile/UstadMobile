package com.ustadmobile.core.io.ext
import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.io.uncompress
import okhttp3.Response
import java.io.InputStream

/**
 * Where a body is present, return a byteStream that will be inflated according to content-encoding
 */
fun Response.bodyAsDecodedByteStream(): InputStream? {
    return body?.byteStream()?.uncompress(CompressionType.byHeaderVal(headers["content-encoding"]))
}
