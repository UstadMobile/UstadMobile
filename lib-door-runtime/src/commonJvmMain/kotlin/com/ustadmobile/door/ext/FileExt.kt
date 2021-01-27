package com.ustadmobile.door.ext

import com.ustadmobile.door.util.NullOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.DigestInputStream
import java.security.MessageDigest


fun File.copyAndGetMd5(dest: OutputStream): ByteArray {
    val messageDigest = MessageDigest.getInstance("MD5")
    val digestInputStream = DigestInputStream(FileInputStream(this), messageDigest)

    digestInputStream.use {
        it.copyTo(dest)
        it.close()
    }

    return messageDigest.digest()
}

val File.md5Sum: ByteArray
    get() = copyAndGetMd5(NullOutputStream())

fun File.copyAndGetMd5(dest: File) = copyAndGetMd5(FileOutputStream(dest))

