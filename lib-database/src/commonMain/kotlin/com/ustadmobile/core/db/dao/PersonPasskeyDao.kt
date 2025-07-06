package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonPasskey
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class PersonPasskeyDao{


    @Insert
    abstract suspend fun insertAsync(personPasskey: PersonPasskey):Long

    @Query(
        """
        SELECT PersonPasskey.ppId
          FROM PersonPasskey
              """
    )
    abstract suspend fun allPasskey(): List<String>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query(
        """
        SELECT * 
          FROM PersonPasskey
         WHERE isRevoked = 0
         AND PersonPasskey.ppPersonUid = :uid
              """
    )
    abstract fun getAllActivePasskeys(uid: Long): Flow<List<PersonPasskey>>

    @Query(
        """
        SELECT * 
          FROM PersonPasskey
         WHERE isRevoked = 0
          AND PersonPasskey.ppPersonUid = :uid
              """
    )
    abstract fun getAllActivePasskeysPaging(uid: Long):  PagingSource<Int, PersonPasskey>

    @Query(
        """
        SELECT *
          FROM PersonPasskey
         WHERE PersonPasskey.ppId = :id 
              """
    )
    abstract suspend fun findPersonPasskeyFromClientDataJson(id: String): PersonPasskey?

    @Query(
        """
        UPDATE PersonPasskey
          set isRevoked = 1
         WHERE PersonPasskey.personPasskeyUid = :uid 
              """
    )
    abstract suspend fun revokePersonPasskey(uid:Long)

}