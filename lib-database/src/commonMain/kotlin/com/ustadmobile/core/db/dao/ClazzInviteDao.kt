package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.db.entities.ClazzInviteWithTimeZone
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Person
import io.ktor.http.HttpMethod


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
    @Query("""
        UPDATE ClazzInvite SET inviteStatus = :status WHERE ClazzInvite.ciUid =:ciUid
    """)
    abstract suspend fun updateInviteStatus(status:Int,ciUid:Long)



}