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
import java.io.IOException
import java.nio.file.Files
import java.util.zip.GZIPOutputStream


class CompressJob {

    constructor() {

        val db = UmAppDatabase.getInstance(Any())
        val entryFileDao = db.containerEntryFileDao
        val containerDao = db.containerDao

        GlobalScope.launch {
            var fileList = entryFileDao.getAllFilesForCompression()
            println("found ${fileList.size} files")
            do {
                fileList.forEach {

                    val sourceFile = File(it.cefPath!!)
                    val destFile = File(sourceFile.parentFile, "tmp")
                    val fileInput = FileInputStream(sourceFile)
                    val fileOutput = FileOutputStream(destFile)
                    val gzipOut = GZIPOutputStream(fileOutput)

                    try {

                        println("sourceFile original length ${sourceFile.length()}")

                        UMIOUtils.readFully(fileInput, gzipOut)

                        UMIOUtils.closeInputStream(fileInput)
                        UMIOUtils.closeOutputStream(fileOutput)
                        UMIOUtils.closeOutputStream(gzipOut)

                        val isDeleted = sourceFile.delete()
                        if (isDeleted) {
                            destFile.renameTo(sourceFile)
                        } else {
                            throw IOException("original source file was not deleted for file ${sourceFile.path}")
                        }

                        it.compression = COMPRESSION_GZIP
                        it.ceCompressedSize = sourceFile.length()

                        println("sourceFile compressed length ${sourceFile.length()}")

                        entryFileDao.updateCompressedFile(it.compression, it.ceCompressedSize, it.cefUid)
                    } catch (io: IOException) {
                        println("error for compressing file ${sourceFile.path}")
                        io.printStackTrace()
                    } finally {
                        UMIOUtils.closeInputStream(fileInput)
                        UMIOUtils.closeOutputStream(fileOutput)
                        UMIOUtils.closeOutputStream(gzipOut)
                    }
                }
                fileList = entryFileDao.getAllFilesForCompression()
                println("found ${fileList.size} more files")

            } while (fileList.isNotEmpty())

            containerDao.updateFileSizeForAllContainers()

        }

    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            CompressJob()
        }

    }

}