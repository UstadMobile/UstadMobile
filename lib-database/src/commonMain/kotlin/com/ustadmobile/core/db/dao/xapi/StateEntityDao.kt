package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.StateEntity

@DoorDao
@Repository
expect abstract class StateEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(stateEntities: List<StateEntity>)

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    /**
     * Retrieve the StateEntity for a singular state retrieval.
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
           AND seHash = :seHash
    """)
    abstract suspend fun getByParams(
        accountPersonUid: Long,
        agentActorUid: Long,
        seHash: Long,
    ): StateEntity?

}