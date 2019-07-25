package com.ustadmobile.core

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sun.misc.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.GZIPOutputStream

class CompressJob {

    fun main(args: Array<String>) {
        CompressJob()
    }

    constructor() {

        val db = UmAppDatabase.getInstance(Any())
        val entryFileDao = db.containerEntryFileDao
        val containerDao = db.containerDao

        GlobalScope.launch {
            var fileList = entryFileDao.getAllFilesForCompression()
            do {
                fileList.forEach {

                    val sourceFile = File(it.cefPath!!)
                    val destFile = File(sourceFile.parentFile, "tmp")

                    val fileInput = FileInputStream(sourceFile)

                    val fileOutput = FileOutputStream(destFile)
                    val gzipOut = GZIPOutputStream(fileOutput)

                    UMIOUtils.readFully(fileInput, gzipOut)

                    UMIOUtils.closeInputStream(fileInput)
                    UMIOUtils.closeOutputStream(fileOutput)
                    UMIOUtils.closeOutputStream(gzipOut)

                    sourceFile.delete()
                    destFile.renameTo(sourceFile)

                    it.compression = COMPRESSION_GZIP
                    it.ceCompressedSize = sourceFile.length()

                    entryFileDao.updateCompressedFile(it.compression, it.ceCompressedSize, it.cefUid)
                }
                fileList = entryFileDao.getAllFilesForCompression()

            } while (fileList.isNotEmpty())

            containerDao.updateFileSizeForAllContainers()

        }

    }

}