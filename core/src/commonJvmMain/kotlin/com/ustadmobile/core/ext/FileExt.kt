package com.ustadmobile.core.ext

import com.ustadmobile.door.util.NullOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun File.copyAndGetMd5(dest: OutputStream, inflate: Boolean = false): ByteArray {
    val messageDigest = MessageDigest.getInstance("MD5")
    val inStream = if(inflate) {
        GZIPInputStream(FileInputStream(this))
    }else {
        FileInputStream(this)
    }

    val digestInputStream = DigestInputStream(inStream, messageDigest)

    digestInputStream.use {
        it.copyTo(dest)
        it.close()
    }

    dest.flush()

    return messageDigest.digest()
}

val File.md5Sum: ByteArray
    get() = copyAndGetMd5(NullOutputStream())

fun File.copyAndGetMd5(dest: File) = FileOutputStream(dest).use {
    copyAndGetMd5(it)
}

fun File.gzipAndGetMd5(dest: File) = GZIPOutputStream(FileOutputStream(dest)).use {
    copyAndGetMd5(it)
}

/**
 * Require that the file have the given extension. If it doesn't then append it to the end. This
 * would not replace any existing extension.
 *
 * If the file already has the given extension, then it is returned as-is.
 */
fun File.requireExtension(extension: String) : File{
    return if(this.extension == extension) {
        this
    }else {
        File(parentFile, "$name.$extension")
    }
}

/**
 * Determine if the given file is a child of another directory
 */
fun File.isChildOf(parent: File): Boolean {
    val absoluteFile = this.absoluteFile
    val absoluteParent = parent.absoluteFile

    var currentFile: File? = absoluteFile
    while(currentFile?.parentFile?.also { currentFile = it } != null) {
        if(currentFile == absoluteParent)
            return true
    }

    return false
}

