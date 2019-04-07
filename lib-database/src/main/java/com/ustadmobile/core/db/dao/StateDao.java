package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.StateEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
@UmRepository
public abstract class StateDao implements SyncableDao<StateEntity, StateDao> {

    @UmQuery("SELECT * FROM StateEntity WHERE stateId = :id LIMIT 1")
    public abstract StateEntity findByStateId(String id);

    @UmQuery("SELECT * FROM StateEntity WHERE agentUid = :agentUid AND activityId = :activityId " +
            "AND registration = :registration AND isactive = 1 AND timestamp > :since")
    public abstract List<StateEntity> findStateIdByAgentAndActivity(long agentUid, String activityId, String registration, String since);

    @UmQuery("UPDATE StateEntity SET isactive = 0 WHERE agentUid = :agentUid AND activityId = :activityId " +
            "AND registration = :registration AND isactive = 1 AND timestamp > :since")
    public abstract void updateStateToInActive(long agentUid, String activityId, String registration, String since);

    @UmQuery("UPDATE StateEntity SET isactive = 0 WHERE stateId = :stateId")
    public abstract void setStateInActive(String stateId);
}
