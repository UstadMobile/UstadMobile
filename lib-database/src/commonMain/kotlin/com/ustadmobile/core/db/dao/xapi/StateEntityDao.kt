package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.StateEntity

@DoorDao
@Repository
expect abstract class StateEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(stateEntities: List<StateEntity>)

    @Query("""
        SELECT StateEntity.*
          FROM StateEntity
         WHERE seActorUid = :agentActorUid
           AND seActivityUid = :activityUid
           AND seStateId = :stateId
           AND (   (:registrationIdHi IS NULL AND :registrationIdLo IS NULL) 
                OR (seRegistrationHi = :registrationIdHi AND seRegistrationLo = :registrationIdLo)
               )
           AND :accountPersonUid = :accountPersonUid     
    """)
    abstract suspend fun getByParams(
        accountPersonUid: Long,
        agentActorUid: Long,
        activityUid: Long,
        registrationIdHi: Long?,
        registrationIdLo: Long?,
        stateId: String,
    ): List<StateEntity>

}