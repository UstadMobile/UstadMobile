package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.AgentEntity

@Dao
@Repository
abstract class AgentDao :BaseDao<AgentEntity> {

    @Query("SELECT * FROM AgentEntity WHERE agentOpenId = :openId OR agentMbox = :mbox " +
            "OR agentMbox_sha1sum = :sha1 OR (agentAccountName = :account AND agentHomePage = :homepage)")
    abstract fun getAgentByAnyId(openId: String? = "", mbox: String? = "", account: String? = "", homepage: String? = "", sha1: String? = ""): AgentEntity?

}
