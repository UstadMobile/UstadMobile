package com.ustadmobile.core.domain.theme

interface UnzipFileUseCase {
    suspend operator fun invoke(zipFilePath: String, destinationDir: String): Result<Unit>
}