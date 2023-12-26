package com.ustadmobile.core.domain.blob.savepicture

/**
 *
 * Enqueues work (via WorkManager on Android, Quartz on JVM, ? on JS) to upload a picture. This is
 * used by the ViewModels when the user saves.
 *
 * Handle saving a picture for the given entity uid / table uid. Will save the picture as a blob,
 * then update the database/repository accordingly.
 */
interface EnqueueSavePictureUseCase {

    suspend fun invoke(
        entityUid: Long,
        tableId: Int,
        pictureUri: String?
    )

}