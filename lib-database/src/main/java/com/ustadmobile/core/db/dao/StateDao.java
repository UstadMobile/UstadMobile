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

    @UmQuery("SELECT * FROM StateEntity WHERE stateId = :id AND agentUid = :agentUid AND activityId = :activityId " +
            "AND registration = :registration AND isactive LIMIT 1")
    public abstract StateEntity findByStateId(String id, long agentUid, String activityId, String registration);

    @UmQuery("SELECT * FROM StateEntity WHERE agentUid = :agentUid AND activityId = :activityId " +
            "AND registration = :registration AND isactive AND timestamp > :since")
    public abstract List<StateEntity> findStateIdByAgentAndActivity(long agentUid, String activityId, String registration, String since);

    @UmQuery("UPDATE StateEntity SET isactive = :isActive WHERE agentUid = :agentUid AND activityId = :activityId " +
            "AND registration = :registration AND isactive")
    public abstract void updateStateToInActive(long agentUid, String activityId, String registration, boolean isActive);

    @UmQuery("UPDATE StateEntity SET isactive = :isActive WHERE stateId = :stateId AND agentUid = :agentUid AND activityId = :activityId " +
            "AND registration = :registration AND isactive")
    public abstract void setStateInActive(String stateId, long agentUid, String activityId, String registration, boolean isActive);
}
