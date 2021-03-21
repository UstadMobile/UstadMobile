package com.ustadmobile.port.sharedse.util


import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object UmFileUtilSe {


    /**
     * Copy input stream to a file
     */
    @JvmStatic
    fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }

    @Throws(IOException::class)
    fun makeTempDir(prefix: String, postfix: String): File {
        val tmpDir = File.createTempFile(prefix, postfix)
        return if (tmpDir.delete() && tmpDir.mkdirs())
            tmpDir
        else
            throw IOException("Could not delete / create tmp dir")
    }

}
