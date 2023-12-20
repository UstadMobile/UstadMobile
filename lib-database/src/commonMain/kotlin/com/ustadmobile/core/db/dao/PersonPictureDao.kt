package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.coroutines.flow.Flow


@DoorDao
@Repository
expect abstract class PersonPictureDao : BaseDao<PersonPicture> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(personPicture: PersonPicture)

    @Query("""SELECT * FROM PersonPicture 
        WHERE personPicturePersonUid = :personUid
        AND CAST(personPictureActive AS INTEGER) = 1
        ORDER BY picTimestamp DESC LIMIT 1""")
    abstract suspend fun findByPersonUidAsync(personUid: Long): PersonPicture?

    @Query("SELECT * FROM PersonPicture where personPicturePersonUid = :personUid ORDER BY " + " picTimestamp DESC LIMIT 1")
    abstract fun findByPersonUidLive(personUid: Long): Flow<PersonPicture?>

    @Query("""
        SELECT * 
          FROM PersonPicture 
         WHERE personPicturePersonUid = :personUid
          AND CAST(personPictureActive AS INTEGER) = 1
        ORDER BY picTimestamp DESC 
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
               personPictureLct = :time
         WHERE personPictureUid = :uid      
    """)
    abstract suspend fun updateUri(uid: Long, uri: String?, time: Long)


}