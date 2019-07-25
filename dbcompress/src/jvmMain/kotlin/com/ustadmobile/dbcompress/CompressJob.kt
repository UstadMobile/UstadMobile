package com.ustadmobile.dbcompress

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.GZIPOutputStream


class CompressJob {

    constructor() {

        val db = UmAppDatabase.getInstance(Any())
        val entryFileDao = db.containerEntryFileDao
        val containerDao = db.containerDao

        runBlocking {
            var fileList = entryFileDao.getAllFilesForCompression()
            println("found ${fileList.size} files")
            do {
                fileList.forEach {

                    val sourceFile = File(it.cefPath!!)
                    val destFile = File(sourceFile.parentFile, "${sourceFile.name}.tmp")
                    val fileInput = FileInputStream(sourceFile)
                    val gzipOut = GZIPOutputStream(FileOutputStream(destFile))

                    try {

                        println("Source file ${sourceFile.name} original length ${sourceFile.length()}, containerEntryFileUid = ${it.cefUid}")

                        UMIOUtils.readFully(fileInput, gzipOut)

                        UMIOUtils.closeInputStream(fileInput)
                        UMIOUtils.closeOutputStream(gzipOut)

                        it.ceCompressedSize = destFile.length()
                        val isDeleted = sourceFile.delete()
                        if (isDeleted) {
                            destFile.renameTo(sourceFile)
                        } else {
                            throw IOException("original source file was not deleted for file ${sourceFile.path}")
                        }


                        it.compression = COMPRESSION_GZIP
                        println("sourceFile compressed length ${it.ceCompressedSize}")

                        entryFileDao.updateCompressedFile(it.compression, it.ceCompressedSize, it.cefUid)
                    } catch (io: IOException) {
                        println("error for compressing file ${sourceFile.path}")
                        io.printStackTrace()
                    } finally {
                        UMIOUtils.closeInputStream(fileInput)
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