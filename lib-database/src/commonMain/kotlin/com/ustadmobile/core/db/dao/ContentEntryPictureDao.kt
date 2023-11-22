package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryPicture


@DoorDao
@Repository
expect abstract class ContentEntryPictureDao : BaseDao<ContentEntryPicture> {


    @Query("""
        SELECT * 
          FROM ContentEntryPicture 
         WHERE cepContentEntryUid = :entryUid
           AND cepActive
      ORDER BY cepTimestamp DESC 
         LIMIT 1
         """)
    abstract suspend fun findByContentEntryUidAsync(entryUid: Long): ContentEntryPicture?

    @Query("""
         SELECT * 
          FROM ContentEntryPicture 
         WHERE cepContentEntryUid = :entryUid
           AND cepActive
      ORDER BY cepTimestamp DESC 
         LIMIT 1
         """)
    abstract fun findByContentEntryUidLive(entryUid: Long): Flow<ContentEntryPicture?>

    @Update
    abstract suspend fun updateAsync(ContentEntryPicture: ContentEntryPicture)


}