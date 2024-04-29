package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryPicture2

@Repository
@DoorDao
expect abstract class ContentEntryPicture2Dao: ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<ContentEntryPicture2>)

    @Query("""
        UPDATE ContentEntryPicture2
           SET cepPictureUri = :uri,
               cepThumbnailUri = :thumbnailUri,
               cepLct = :time
         WHERE cepUid = :uid  
    """)
    abstract override suspend fun updateUri(
        uid: Long, uri: String?, thumbnailUri: String?, time: Long
    )
}