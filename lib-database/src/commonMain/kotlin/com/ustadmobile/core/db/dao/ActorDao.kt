package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.xapi.ActorEntity

@DoorDao
@Repository
expect abstract class ActorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entities: List<ActorEntity>)

    @Query("""
        UPDATE ActorEntity
           SET actorName = :name,
               actorLct = :updateTime
         WHERE actorUid = :uid     
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
    abstract suspend fun findByUidAsync(uid: Long): ActorEntity

}
