package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.model.FileToZip
import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.flow.Flow

interface ZipFileUseCase {
    suspend operator fun invoke(filesToZip: List<FileToZip>, outputPath: String): Flow<ZipProgress>
}
expect fun createZipFileUseCase(): ZipFileUseCase