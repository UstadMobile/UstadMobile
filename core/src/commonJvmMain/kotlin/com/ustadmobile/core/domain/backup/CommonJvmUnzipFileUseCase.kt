package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

abstract class CommonJvmUnzipFileUseCase : UnzipFileUseCase {

    protected abstract fun openInputStream(path: String): InputStream
    protected abstract fun getOutputDirectory(): String

    final override fun invoke(zipFilePath: String): Flow<ZipProgress> = flow {
        val outputDirectory = getOutputDirectory()
        val outputDir = File(outputDirectory)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        openInputStream(zipFilePath).use { inputStream ->
            ZipInputStream(inputStream.buffered()).use { zipIn ->
                var entry = zipIn.nextEntry
                var entriesProcessed = 0
                val totalEntries = countEntries(zipIn)

                while (entry != null) {
                    val filePath = File(outputDirectory, entry.name)

                    if (!entry.isDirectory) {
                        filePath.parentFile?.takeIf { !it.exists() }?.mkdirs()

                        try {
                            filePath.outputStream().use { output ->
                                zipIn.copyTo(output)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        filePath.mkdirs()
                    }

                    zipIn.closeEntry()
                    entriesProcessed++
                    emit(ZipProgress(entry.name, totalEntries, entriesProcessed.toFloat() / totalEntries))
                    entry = zipIn.nextEntry
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun countEntries(zipIn: ZipInputStream): Int {
        var count = 0
        while (zipIn.nextEntry != null) {
            count++
            zipIn.closeEntry()
        }
        return count
    }
}
