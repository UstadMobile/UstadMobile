package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.flow.Flow

interface UnzipFileUseCase {
    suspend operator fun invoke(zipFilePath: String, outputDirectory: String): Flow<ZipProgress>
}

expect fun createUnzipFileUseCase(): UnzipFileUseCase