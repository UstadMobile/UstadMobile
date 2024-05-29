package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.ActorUidEtagAndLastMod
import com.ustadmobile.lib.db.entities.xapi.ActorEntity

@DoorDao
@Repository
expect abstract class ActorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entities: List<ActorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<ActorEntity>)

    @Query("""
        UPDATE ActorEntity
           SET actorName = :name,
               actorLct = :updateTime
         WHERE actorUid = :uid
           AND ActorEntity.actorName != :name
    """)
    abstract suspend fun updateIfNameChanged(
        uid: Long,
        name: String?,
        updateTime: Long,
    )

    @Query("""
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ActorEntity?

    @Query("""
        SELECT ActorEntity.actorUid, ActorEntity.actorEtag, ActorEntity.actorLct
          FROM ActorEntity
         WHERE ActorEntity.actorUid IN (:uidList)
    """)
    abstract suspend fun findUidAndEtagByListAsync(uidList: List<Long>): List<ActorUidEtagAndLastMod>

    @Query("""
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorUid IN (
               SELECT GroupMemberActorJoin.gmajMemberActorUid
                 FROM GroupMemberActorJoin
                WHERE GroupMemberActorJoin.gmajGroupActorUid = :groupActorUid
                  AND GroupMemberActorJoin.gmajLastMod = (
                      SELECT GroupActorEntity.actorLct
                        FROM ActorEntity GroupActorEntity
                       WHERE GroupActorEntity.actorUid = :groupActorUid)
              ) 
    """)
    abstract suspend fun findGroupMembers(groupActorUid: Long): List<ActorEntity>



}