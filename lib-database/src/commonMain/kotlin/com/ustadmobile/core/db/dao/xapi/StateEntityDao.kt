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

    /**
     * Retrieve StateEntities to answer a GET request for a specific state document.
     *
     * @param accountPersonUid personUid for the session / active user. This MUST be the personUid
     *        for the agentActorUid. Used for access control.
     * @param agentActorUid actor uid
     */
    @Query("""
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :agentActorUid) = :accountPersonUid
           AND seActorUid = :agentActorUid
           AND seActivityUid = :activityUid
           AND seStateId = :stateId
           AND (   (:registrationIdHi IS NULL AND :registrationIdLo IS NULL) 
                OR (seRegistrationHi = :registrationIdHi AND seRegistrationLo = :registrationIdLo)
               )
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