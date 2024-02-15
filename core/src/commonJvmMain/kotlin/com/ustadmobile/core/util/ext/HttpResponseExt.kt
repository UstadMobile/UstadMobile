package com.ustadmobile.core.util.ext

import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.io.uncompress
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream

actual suspend fun HttpResponse.bodyAsDecodedText(): String {
    val contentTypeCompression = CompressionType.byHeaderVal(headers["content-encoding"])
    return bodyAsChannel().toInputStream().uncompress(contentTypeCompression).reader().use {
        it.readText()
    }
}
