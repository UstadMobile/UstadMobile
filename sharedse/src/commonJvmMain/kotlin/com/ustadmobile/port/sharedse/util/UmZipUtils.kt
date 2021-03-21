package com.ustadmobile.port.sharedse.util


import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object UmZipUtils {

    @Throws(IOException::class)
    fun unzip(zipFile: File, destDir: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
            var zipEntry: ZipEntry?
            while (zipIn.nextEntry.also { zipEntry = it } != null) {

                val fileName = zipEntry!!.name
                val fileToCreate = File(destDir, fileName)

                val dirToCreate = if (zipEntry!!.isDirectory) fileToCreate else fileToCreate.parentFile
                if (!dirToCreate.isDirectory) {
                    if (!dirToCreate.mkdirs()) {
                        throw RuntimeException("Could not create directory to extract to: " + fileToCreate.parentFile)
                    }
                }
                if (!zipEntry!!.isDirectory) {
                    FileOutputStream(fileToCreate).use {
                        zipIn.copyTo(it)
                    }
                }
            }
        }
    }

}
