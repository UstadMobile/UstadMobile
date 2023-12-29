package com.ustadmobile.core.domain.blob.savepicture


class EnqueueSavePictureUseCaseJs(
    private val savePictureUseCase: SavePictureUseCase
): EnqueueSavePictureUseCase {
    override suspend fun invoke(entityUid: Long, tableId: Int, pictureUri: String?) {
        savePictureUseCase(entityUid, tableId, pictureUri)
    }
}