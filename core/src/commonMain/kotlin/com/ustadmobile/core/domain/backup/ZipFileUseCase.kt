package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.flow.Flow

interface ZipFileUseCase {
     operator fun invoke(filesToZip: List<FileToZip>, zipFilePath: String): Flow<ZipProgress>
}

data class FileToZip(
    val inputUri: String,
    val pathInZip: String
)

