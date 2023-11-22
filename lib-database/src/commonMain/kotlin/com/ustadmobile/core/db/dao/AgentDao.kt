package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AgentEntity
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Person.Companion.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Role

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
