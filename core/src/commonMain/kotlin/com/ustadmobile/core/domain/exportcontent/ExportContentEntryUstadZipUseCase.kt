package com.ustadmobile.core.domain.export


import kotlinx.coroutines.flow.Flow

interface ExportContentEntryUstadZipUseCase {
    suspend operator fun invoke(
        contentEntryUid: Long,
        destZipFilePath: String,
        progressListener: (ExportProgress) -> Unit
    )

    fun getOutputDirectory(): String
}


data class ExportProgress(
    val currentItem: String,
    val totalItems: Int,
    val progress: Float
)
