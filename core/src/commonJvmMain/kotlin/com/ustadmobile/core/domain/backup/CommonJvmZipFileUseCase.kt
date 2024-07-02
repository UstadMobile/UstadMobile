package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

abstract class CommonJvmZipFileUseCase : ZipFileUseCase {

    final override fun invoke(filesToZip: List<FileToZip>, zipFilePath: String): Flow<ZipProgress> = flow {
        val zipFile = createOutputFile(zipFilePath)

        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            val totalFiles = filesToZip.size

            filesToZip.forEachIndexed { index, fileToZip ->
                val entry = ZipEntry(fileToZip.pathInZip)
                zipOut.putNextEntry(entry)

                val inputStream = openInputStream(fileToZip.inputUri)
                inputStream.use { input ->
                    input.copyTo(zipOut)
                }

                zipOut.closeEntry()
                emit(ZipProgress(fileToZip.pathInZip, totalFiles, (index + 1).toFloat() / totalFiles))
            }
        }
    }.flowOn(Dispatchers.IO)

    protected abstract fun openInputStream(uri: String): InputStream
    protected abstract fun createOutputFile(path: String): File
}
