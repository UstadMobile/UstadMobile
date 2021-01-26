package com.ustadmobile.door.ext
import java.io.FileOutputStream
import java.io.File
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Write the given InputStream to a file, flush, and close
 */
fun InputStream.writeToFile(file: File) {
    use { inStream ->
        FileOutputStream(file).use { outStream ->
            inStream.copyTo(outStream)
            outStream.flush()
        }
    }
}

/**
 * Write the given InputStream to a file, flush, close,
 * and return the MD5 of the data that was written.
 */
fun InputStream.writeToFileAndGetMd5(destFile: File) : ByteArray {
    val messageDigest = MessageDigest.getInstance("MD5")
    DigestInputStream(this, messageDigest).use { inStream ->
        FileOutputStream(destFile).use { outStream ->
            inStream.copyTo(outStream)
            outStream.flush()
        }
    }

    return messageDigest.digest()
}
