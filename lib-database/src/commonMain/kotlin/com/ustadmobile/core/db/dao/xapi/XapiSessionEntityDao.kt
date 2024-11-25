package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.PersonAndAttemptInfo
import com.ustadmobile.lib.db.composites.StatementAndPersonAndPicture
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

@DoorDao
@Repository
expect abstract class XapiSessionEntityDao {

    @Insert
    abstract suspend fun insertAsync(xapiSessionEntity: XapiSessionEntity)

    @Query(
        """
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseUid = :uid
    """
    )
    abstract suspend fun findByUidAsync(uid: Long): XapiSessionEntity?

    @Query(
        """
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
    """
    )
    abstract suspend fun findSession(): XapiSessionEntity?

    @Query(
        """
        UPDATE XapiSessionEntity
           SET xseCompleted = :completed,
               xseLastMod = :time
         WHERE xseUid = :xseUid

    """
    )
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
                functionName = "findByUidAndPersonUidAsync",
                functionDao = ActorDao::class,
            ),
        )
    )
    @Query(
        """
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
    """
    )
    abstract suspend fun findMostRecentSessionByActorAndActivity(
        accountPersonUid: Long,
        actorUid: Long,
        xseRootActivityUid: Long,
        contentEntryVersionUid: Long,
        clazzUid: Long,
    ): XapiSessionEntity?


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "getAttemptList"
            ),
        )
    )
    @Query(
        """
    SELECT Person.*, PersonPicture.*,
           (SELECT COUNT(XapiSessionEntity.xseUid)
            FROM XapiSessionEntity
            WHERE XapiSessionEntity.xseAccountPersonUid = Person.personUid
              AND XapiSessionEntity.xseContentEntryUid = :contentEntryUid) AS numberAttempts
    FROM Person
    LEFT JOIN PersonPicture 
                ON PersonPicture.personPictureUid = Person.personUid
    WHERE numberAttempts >= 1
               
"""
    )
    abstract fun getAttemptList(contentEntryUid: Long): PagingSource<Int, PersonAndAttemptInfo>

    @HttpAccessible
    @Query(
        """
    SELECT *FROM StatementEntity
    WHERE StatementEntity.statementActorPersonUid = :personUid
    And StatementEntity.statementContentEntryUid=:contentEntryUid
    AND ROWID IN 
    (SELECT MIN(ROWID)
    FROM StatementEntity
    WHERE StatementEntity.statementActorPersonUid = :personUid
    GROUP BY StatementEntity.statementIdHi, StatementEntity.statementIdLo)
"""
    )
    abstract fun getSessionList(
        contentEntryUid: Long,
        personUid: Long
    ): PagingSource<Int, StatementEntity>


    @HttpAccessible
    @Query("""
       SELECT StatementEntity.*, Person.*, PersonPicture.*
       FROM StatementEntity
       JOIN Person
            ON Person.personUid = StatementEntity.statementActorPersonUid
       LEFT JOIN PersonPicture
            ON PersonPicture.personPictureUid = Person.personUid 
       WHERE (StatementEntity.statementIdHi, StatementEntity.statementIdLo) IN (
           SELECT StatementEntity.statementIdHi, StatementEntity.statementIdLo
             FROM StatementEntity
            WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
              AND StatementEntity.statementActorPersonUid = Person.personUid
         ORDER BY StatementEntity.resultDuration DESC      
            LIMIT 1 
       )
""")
abstract   fun findPersonsWithAttempts(contentEntryUid: Long):
            PagingSource<Int, StatementAndPersonAndPicture>
}
