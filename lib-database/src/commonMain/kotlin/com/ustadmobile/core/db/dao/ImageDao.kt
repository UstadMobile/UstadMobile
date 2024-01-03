package com.ustadmobile.core.db.dao

/**
 * Interface that is used by SavePictureUseCase
 */
interface ImageDao {

    suspend fun updateUri(
        uid: Long, uri: String?, thumbnailUri: String?, time: Long
    )

}