package com.ustadmobile.port.sharedse.util

import com.ustadmobile.core.util.UMIOUtils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object UmZipUtils {

    @Throws(IOException::class)
    fun unzip(zipFile: File, destDir: File) {
        var entryOut: OutputStream? = null
        try {
            ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
                var zipEntry: ZipEntry
                while ((zipEntry = zipIn.nextEntry) != null) {

                    val fileName = zipEntry.name
                    val fileToCreate = File(destDir, fileName)

                    val dirToCreate = if (zipEntry.isDirectory) fileToCreate else fileToCreate.parentFile
                    if (!dirToCreate.isDirectory) {
                        if (!dirToCreate.mkdirs()) {
                            throw RuntimeException("Could not create directory to extract to: " + fileToCreate.parentFile)
                        }
                    }
                    if (!zipEntry.isDirectory) {
                        entryOut = FileOutputStream(fileToCreate)
                        UMIOUtils.readFully(zipIn, entryOut!!)
                        entryOut!!.close()
                    }
                }
            }
        } finally {
            UMIOUtils.closeQuietly(entryOut)
        }

    }

}
