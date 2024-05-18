package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AgentEntity

@DoorDao
@Repository
expect abstract class AgentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entities: List<AgentEntity>)

    @Query("""
        SELECT AgentEntity.*
          FROM AgentEntity
         WHERE AgentEntity.agentUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): AgentEntity

}
