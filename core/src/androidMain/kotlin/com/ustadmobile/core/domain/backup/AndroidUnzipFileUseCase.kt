package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class AndroidUnzipFileUseCase : UnzipFileUseCase {
    override suspend fun invoke(zipFilePath: String, outputDirectory: String): Flow<ZipProgress> = flow {
        println("Starting unzip process. Zip file: $zipFilePath, Output directory: $outputDirectory")

        withContext(Dispatchers.IO) {
            try {
                val zipFile = File(zipFilePath)
                val outputDir = File(outputDirectory)
                if (!outputDir.exists()) {
                    println("Creating output directory")
                    outputDir.mkdirs()
                }

                ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
                    var entry = zipIn.nextEntry
                    var entriesProcessed = 0
                    val totalEntries = zipFile.inputStream().use { ZipInputStream(it).count() }

                    while (entry != null) {
                        val filePath = outputDirectory + File.separator + entry.name
                        println("Extracting: $filePath")

                        if (!entry.isDirectory) {
                            File(filePath).outputStream().use { output ->
                                zipIn.copyTo(output)
                            }
                        } else {
                            File(filePath).mkdirs()
                        }
                        zipIn.closeEntry()
                        entriesProcessed++
                        val progress = entriesProcessed.toFloat() / totalEntries
                        println("Progress: $progress")
                        emit(ZipProgress(entry.name, totalEntries, progress))
                        entry = zipIn.nextEntry
                    }
                }
                println("Unzip process completed successfully")
            } catch (e: Exception) {
                println("Error during unzip process: ${e.message}")
                e.printStackTrace()
                throw e // Re-throw the exception to be caught in the ViewModel
            }
        }
    }

    private fun ZipInputStream.count(): Int {
        var count = 0
        while (nextEntry != null) {
            count++
            closeEntry()
        }
        return count
    }
}

actual fun createUnzipFileUseCase(): UnzipFileUseCase = AndroidUnzipFileUseCase()