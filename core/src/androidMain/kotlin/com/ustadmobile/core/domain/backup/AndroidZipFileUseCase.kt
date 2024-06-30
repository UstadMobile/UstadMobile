package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.model.FileToZip
import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AndroidZipFileUseCase : ZipFileUseCase {
    override suspend fun invoke(filesToZip: List<FileToZip>, outputPath: String): Flow<ZipProgress> = flow {
        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(outputPath)).use { zipOut ->
                filesToZip.forEachIndexed { index, fileToZip ->
                    val inputFile = File(fileToZip.pathInZip)
                    val entry = ZipEntry(fileToZip.pathInZip)
                    zipOut.putNextEntry(entry)
                    FileInputStream(inputFile).use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                    emit(ZipProgress(fileToZip.pathInZip, filesToZip.size, (index + 1f) / filesToZip.size))
                }
            }
        }
    }
}
actual fun createZipFileUseCase(): ZipFileUseCase = AndroidZipFileUseCase()