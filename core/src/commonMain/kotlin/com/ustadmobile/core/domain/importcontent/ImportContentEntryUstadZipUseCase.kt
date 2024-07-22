package com.ustadmobile.core.domain.import

interface ImportContentEntryUstadZipUseCase {
    suspend operator fun invoke(
        sourceZipFilePath: String,
        progressListener: (ImportProgress) -> Unit
    )
}

data class ImportProgress(
    val currentItem: String,
    val totalItems: Int,
    val progress: Float
)
