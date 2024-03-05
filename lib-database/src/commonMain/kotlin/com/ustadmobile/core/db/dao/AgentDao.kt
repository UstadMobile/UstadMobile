package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AgentEntity

@DoorDao
@Repository
expect abstract class AgentDao :BaseDao<AgentEntity> {



    @Query("SELECT * FROM AgentEntity WHERE agentOpenId = :openId OR agentMbox = :mbox " +
            "OR agentMbox_sha1sum = :sha1 OR (agentAccountName = :account AND agentHomePage = :homepage)")
    abstract fun getAgentByAnyId(openId: String? = "", mbox: String? = "", account: String? = "", homepage: String? = "", sha1: String? = ""): AgentEntity?


    @Query("""
        SELECT *
          FROM AgentEntity
         WHERE agentAccountName = :username 
           AND agentHomePage = :endpoint
    """)
    abstract suspend fun getAgentFromPersonUsername(endpoint: String, username: String): AgentEntity?

}
