package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.ClazzInviteAndClazz
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.db.composites.ClazzInviteWithTimeZone
import kotlinx.coroutines.flow.Flow



@DoorDao
@Repository
expect abstract class ClazzInviteDao : BaseDao<ClazzInvite> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replace(entity: ClazzInvite): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(entity: List<ClazzInvite>)


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("SELECT * FROM ClazzInvite")
    abstract suspend fun findInviteAsync(): List<ClazzInvite>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query(
        """
    SELECT ClazzInvite.*, 
           COALESCE(Clazz.clazzTimeZone, 'UTC') AS timeZone
      FROM ClazzInvite
           LEFT JOIN Clazz 
                     ON Clazz.clazzUid = ClazzInvite.ciClazzUid
     WHERE ClazzInvite.inviteToken = :inviteTokenUid
"""
    )
    abstract suspend fun findClazzInviteEntityForInviteToken(inviteTokenUid: String): ClazzInviteWithTimeZone?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query(
    """
    SELECT ClazzInvite.*, Clazz.*
      FROM ClazzInvite
           JOIN Clazz 
                ON Clazz.clazzUid = ClazzInvite.ciClazzUid
     WHERE ClazzInvite.inviteToken = :inviteTokenUid
    """)
    abstract fun findClazzInviteEntityForInviteTokenAsFlow(
        inviteTokenUid: String
    ): Flow<ClazzInviteAndClazz?>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        UPDATE ClazzInvite 
           SET inviteStatus = :status,
               inviteLct = :updateTime
         WHERE ClazzInvite.ciUid =:ciUid
    """)
    abstract suspend fun updateInviteStatus(
        status:Int,
        ciUid:Long,
        updateTime: Long,
    )


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""SELECT * FROM ClazzInvite 
                 WHERE ciPersonUid = :ciPersonUid AND ciClazzUid = :clazzUid 
              AND inviteExpire > :currentTime AND inviteStatus = 0""")
    abstract fun findPendingInviteByPersonUid(
        ciPersonUid:Long,
        clazzUid: Long,
        currentTime: Long
    ): PagingSource<Int, ClazzInvite>


    @Query("""
        UPDATE ClazzInvite 
          SET inviteStatus = 3
        WHERE inviteContact = :inviteContact""")
    abstract suspend fun updateClazzInviteToRevokeInvite(inviteContact: String): Int


    @Query("""SELECT * FROM ClazzInvite WHERE inviteContact = :inviteContact""")
    abstract suspend fun findClazzInviteFromContact(inviteContact: String): ClazzInvite

}