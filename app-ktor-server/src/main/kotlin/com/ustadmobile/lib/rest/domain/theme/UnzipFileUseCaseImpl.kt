package com.ustadmobile.lib.rest.domain.theme

import com.ustadmobile.core.domain.theme.UnzipFileUseCase
import io.github.aakira.napier.Napier
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
                return Result.failure(Exception("❌ ZIP file does not exist: ${file.absolutePath}"))
            }

            Napier.d("Extracting ZIP from: ${file.absolutePath}")

            val outputDir = File(URI(destinationDir).path)
            outputDir.mkdirs()

            val zipFile = ZipFile(file)
            zipFile.use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val outputFile = File(outputDir, entry.name)
                    if (entry.isDirectory) {
                        outputFile.mkdirs()
                    } else {
                        outputFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    Napier.d("Extracted file: ${outputFile.absolutePath}")
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