package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class JvmUnzipFileUseCase : UnzipFileUseCase {
    override suspend fun invoke(zipFilePath: String, outputDirectory: String): Flow<ZipProgress> = flow {
        withContext(Dispatchers.IO) {
            val zipFile = File(zipFilePath)
            val outputDir = File(outputDirectory)
            if (!outputDir.exists()) outputDir.mkdirs()

            ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
                var entry = zipIn.nextEntry
                var entriesProcessed = 0
                val totalEntries = zipIn.count()

                while (entry != null) {
                    val filePath = outputDirectory + File.separator + entry.name
                    if (!entry.isDirectory) {
                        File(filePath).outputStream().use { output ->
                            zipIn.copyTo(output)
                        }
                    } else {
                        File(filePath).mkdirs()
                    }
                    zipIn.closeEntry()
                    entriesProcessed++
                    emit(ZipProgress(entry.name, totalEntries, entriesProcessed.toFloat() / totalEntries))
                    entry = zipIn.nextEntry
                }
            }
        }
    }

    private fun ZipInputStream.count(): Int {
        var count = 0
        while (nextEntry != null) count++
        return count
    }
}

actual fun createUnzipFileUseCase(): UnzipFileUseCase = JvmUnzipFileUseCase()