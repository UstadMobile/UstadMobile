package com.ustadmobile.core.domain.blob.savepicture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class EnqueueSavePictureUseCaseJs(
    private val savePictureUseCase: SavePictureUseCase
): EnqueueSavePictureUseCase {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    override suspend fun invoke(entityUid: Long, tableId: Int, pictureUri: String?) {
        scope.launch {
            savePictureUseCase(entityUid, tableId, pictureUri)
        }
    }
}