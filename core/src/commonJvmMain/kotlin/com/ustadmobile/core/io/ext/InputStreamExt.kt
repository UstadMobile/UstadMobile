package com.ustadmobile.core.io.ext

import java.io.InputStream
import java.security.MessageDigest


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
