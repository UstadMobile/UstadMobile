package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.AgentEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
@UmRepository
public abstract class AgentDao implements SyncableDao<AgentEntity, AgentDao> {

    @UmQuery("SELECT * FROM AgentEntity WHERE agentOpenId = :openId OR agentMbox = :mbox " +
            "OR agentMbox_sha1sum = :sha1 OR (agentAccountName = :account AND agentHomePage = :homepage)")
    public abstract AgentEntity getAgentByAnyId(String openId, String mbox, String account, String homepage, String sha1);

}
