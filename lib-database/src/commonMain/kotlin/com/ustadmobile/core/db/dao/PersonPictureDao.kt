package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.coroutines.flow.Flow


@DoorDao
@Repository
expect abstract class PersonPictureDao : BaseDao<PersonPicture>, ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(personPicture: PersonPicture)

    @Query("""
        SELECT * 
          FROM PersonPicture 
         WHERE personPictureUid = :personUid
           AND CAST(personPictureActive AS INTEGER) = 1
         """)
    abstract suspend fun findByPersonUidAsync(personUid: Long): PersonPicture?

    @HttpAccessible
    @Query("""
         SELECT * 
           FROM PersonPicture 
          WHERE personPictureUid = :personUid 
          """)
    abstract fun findByPersonUidLive(personUid: Long): Flow<PersonPicture?>

    @Query("""
        SELECT * 
          FROM PersonPicture 
         WHERE personPictureUid = :personUid
          AND CAST(personPictureActive AS INTEGER) = 1
        LIMIT 1
        """)
    abstract fun findByPersonUidAsFlow(personUid: Long): Flow<PersonPicture?>

    @Update
    abstract suspend fun updateAsync(personPicture: PersonPicture)

    @Query("""
        UPDATE PersonPicture
           SET personPictureLct = :time
         WHERE personPictureUid = :uid   
    """)
    abstract suspend fun updateLct(uid: Long, time: Long)

    @Query("""
        UPDATE PersonPicture
           SET personPictureUri = :uri,
               personPictureThumbnailUri = :thumbnailUri,
               personPictureLct = :time
         WHERE personPictureUid = :uid      
    """)
    abstract override suspend fun updateUri(
        uid: Long,
        uri: String?,
        thumbnailUri: String?,
        time: Long
    )


    @Query("""
        UPDATE TransferJobItem
           SET tjiEntityEtag = 
               (SELECT personPictureLct
                  FROM PersonPicture
                 WHERE personPictureUid = :entityUid)
         WHERE tjiUid = :transferJobItemUid      
    """)
    abstract suspend fun updateTransferJobItemEtag(
        entityUid: Long,
        transferJobItemUid: Int
    )


}