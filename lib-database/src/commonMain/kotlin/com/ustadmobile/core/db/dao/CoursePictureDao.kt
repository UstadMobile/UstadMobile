package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CoursePicture


@DoorDao
@Repository
expect abstract class CoursePictureDao: ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(coursePicture: CoursePicture)


    @Query("""
        UPDATE CoursePicture
           SET coursePictureUri = :uri,
               coursePictureThumbnailUri = :thumbnailUri,
               coursePictureLct = :time
        WHERE coursePictureUid = :uid       
    """)
    abstract override suspend fun updateUri(
        uid: Long,
        uri: String?,
        thumbnailUri: String?,
        time: Long
    )

}