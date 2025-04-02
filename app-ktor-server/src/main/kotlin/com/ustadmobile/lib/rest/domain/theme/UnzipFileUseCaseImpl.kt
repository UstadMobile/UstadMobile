package com.ustadmobile.lib.rest.domain.theme

import com.ustadmobile.core.domain.theme.UnzipFileUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.util.zip.ZipFile

class UnzipFileUseCaseImpl : UnzipFileUseCase {
    override suspend fun invoke(zipFilePath: String, destinationDir: String): Result<Unit> {
        return try {
            val file = if (zipFilePath.startsWith("file:")) {
                File(URI(zipFilePath))
            } else {
                File(zipFilePath)
            }

            if (!file.exists()) {
                Napier.e("ZIP file does not exist: ${file.absolutePath}")
                return Result.failure(Exception("ZIP file does not exist: ${file.absolutePath}"))
            }

            Napier.d("Extracting ZIP from: ${file.absolutePath}")
            Napier.d("ZIP file size: ${file.length()} bytes")
            Napier.d("ZIP file readable: ${file.canRead()}")

            val outputDir = File(URI(destinationDir).path)
            outputDir.mkdirs()
            Napier.d("Output directory: ${outputDir.absolutePath}")
            Napier.d("Output directory exists: ${outputDir.exists()}")
            Napier.d("Output directory is writable: ${outputDir.canWrite()}")

            val zipFile = withContext(Dispatchers.IO) {
                ZipFile(file)
            }
            val entryCount = zipFile.entries().asSequence().count()
            Napier.d("Found $entryCount entries in ZIP file")

            zipFile.use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val outputFile = File(outputDir, entry.name)
                    if (entry.isDirectory) {
                        outputFile.mkdirs()
                        Napier.d("Created directory: ${outputFile.absolutePath}")
                    } else {
                        outputFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        Napier.d("Extracted file: ${outputFile.absolutePath}")
                    }
                }
            }

            Napier.d("Extraction complete! Extracted to: ${outputDir.absolutePath}")
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Extraction failed: ${e.message}")
            Result.failure(e)
        }
    }
}