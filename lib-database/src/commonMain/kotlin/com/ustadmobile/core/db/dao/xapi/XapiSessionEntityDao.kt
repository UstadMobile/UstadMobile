package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

@DoorDao
@Repository
expect abstract class XapiSessionEntityDao {

    @Insert
    abstract suspend fun insertAsync(xapiSessionEntity: XapiSessionEntity)

    @Query("""
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): XapiSessionEntity?

    @Query("""
        UPDATE XapiSessionEntity
           SET xseCompleted = :completed,
               xseLastMod = :time
         WHERE xseUid = :xseUid

    """)
    abstract suspend fun updateLatestAsComplete(
        completed: Boolean,
        time: Long,
        xseUid: Long,
    )

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findMostRecentSessionByActorAndActivity"
            ),
            HttpServerFunctionCall(
                functionName ="findByUidAndPersonUidAsync",
                functionDao = ActorDao::class,
            ),
        )
    )
    @Query("""
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseRootActivityUid = :xseRootActivityUid
           AND XapiSessionEntity.xseActorUid = :actorUid
           AND XapiSessionEntity.xseContentEntryVersionUid = :contentEntryVersionUid
           AND XapiSessionEntity.xseClazzUid = :clazzUid
           AND EXISTS(
               SELECT 1
                 FROM ActorEntity
                WHERE ActorEntity.actorUid = :actorUid
                  AND ActorEntity.actorPersonUid = :accountPersonUid)     
    """)
    abstract suspend fun findMostRecentSessionByActorAndActivity(
        accountPersonUid: Long,
        actorUid: Long,
        xseRootActivityUid: Long,
        contentEntryVersionUid: Long,
        clazzUid: Long,
    ): XapiSessionEntity?

}