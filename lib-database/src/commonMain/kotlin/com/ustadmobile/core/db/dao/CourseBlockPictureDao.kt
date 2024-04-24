package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.CourseBlockPicture

@Repository
@DoorDao
expect abstract class CourseBlockPictureDao: ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<CourseBlockPicture>)

    @Query("""
        UPDATE CourseBlockPicture
           SET cbpPictureUri = :uri,
               cbpThumbnailUri = :thumbnailUri,
               cbpLct = :time
         WHERE cbpUid = :uid  
    """)
    override suspend fun updateUri(
        uid: Long, uri: String?, thumbnailUri: String?, time: Long
    )

}