package com.ustadmobile.core.io.ext

import com.ustadmobile.core.domain.compress.CompressionType
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream


fun InputStream.readString(): String{
    return this.bufferedReader().use { it.readText() }
}

fun InputStream.readSha256(): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192)
    var bytesRead: Int
    while(read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
    }

    return digest.digest()
}


fun InputStream.uncompress(
    compressionType: CompressionType
): InputStream {
    return when(compressionType) {
        CompressionType.NONE -> this
        CompressionType.GZIP -> GZIPInputStream(this)
    }
}

