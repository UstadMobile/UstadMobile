package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.xapi.StateIdAndLastModified
import com.ustadmobile.lib.db.entities.xapi.StateEntity

@DoorDao
@Repository
expect abstract class StateEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(stateEntities: List<StateEntity>)

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByActorAndHash",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true"
                    )
                )
            )
        )
    )
    /**
     * Retrieve the StateEntity for a singular state retrieval.
     *
     * @param accountPersonUid personUid for the session / active user. This MUST be the personUid
     *        for the actorUid. Used for access control.
     * @param actorUid actor uid
     */
    @Query("""
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seHash = :seHash
           AND (   CAST(:includeDeleted AS INTEGER) = 1 
                OR CAST(StateEntity.seDeleted AS INTEGER) = 0)
    """)
    abstract suspend fun findByActorAndHash(
        accountPersonUid: Long,
        actorUid: Long,
        seHash: Long,
        includeDeleted: Boolean,
    ): StateEntity?


    @HttpAccessible
    @Query("""
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid
           AND (:modifiedSince = 0 OR StateEntity.seLastMod > :modifiedSince)
           AND ((    :registrationUuidHi IS NULL
                 AND StateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND StateEntity.seRegistrationLo IS NULL)
             OR (    StateEntity.seRegistrationHi = :registrationUuidHi 
                 AND StateEntity.seRegistrationLo = :registrationUuidLo))
           AND StateEntity.seStateId IS NOT NULL  
    """)
    abstract suspend fun findByAgentAndActivity(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
        modifiedSince: Long,
    ): List<StateEntity>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByAgentAndActivity"
            ),
            HttpServerFunctionCall(
                functionName = "findByUidAndPersonUidAsync",
                functionDao = ActorDao::class
            )
        )
    )
    @Query("""
        SELECT StateEntity.seStateId, StateEntity.seLastMod
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid
           AND (:modifiedSince = 0 OR StateEntity.seLastMod > :modifiedSince)
           AND ((    :registrationUuidHi IS NULL
                 AND StateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND StateEntity.seRegistrationLo IS NULL)
             OR (    StateEntity.seRegistrationHi = :registrationUuidHi 
                 AND StateEntity.seRegistrationLo = :registrationUuidLo))
           AND StateEntity.seStateId IS NOT NULL 
           AND CAST(StateEntity.seDeleted AS INTEGER) = 0      
    """)
    abstract suspend fun getStateIds(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
        modifiedSince: Long,
    ): List<StateIdAndLastModified>


    @Query("""
        SELECT StateEntity.*
          FROM StateEntity
         WHERE (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid) = :accountPersonUid
           AND seActorUid = :actorUid
           AND seActivityUid = :seActivityUid 
           AND ((    :registrationUuidHi IS NULL
                 AND StateEntity.seRegistrationHi IS NULL
                 AND :registrationUuidLo IS NULL 
                 AND StateEntity.seRegistrationLo IS NULL)
             OR (    StateEntity.seRegistrationHi = :registrationUuidHi 
                 AND StateEntity.seRegistrationLo = :registrationUuidLo))
           AND StateEntity.seH5PSubContentId IS NOT NULL      
           AND CAST(StateEntity.seH5PPreloaded AS INTEGER) = 1      
    """)
    abstract suspend fun getH5PPreload(
        accountPersonUid: Long,
        actorUid: Long,
        seActivityUid: Long,
        registrationUuidHi: Long?,
        registrationUuidLo: Long?,
    ): List<StateEntity>

}