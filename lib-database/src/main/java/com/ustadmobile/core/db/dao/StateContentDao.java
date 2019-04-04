package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.StateContentEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao
@UmRepository
public abstract class StateContentDao implements SyncableDao<StateContentEntity, StateContentDao> {

    @UmQuery("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :id")
    public abstract List<StateContentEntity> findAllStateContentWithStateUid(long id);

    @UmQuery("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :stateUid AND stateContentKey = :key")
    public abstract StateContentEntity findStateContentByKeyAndStateUid(String key, long stateUid);

    @UmQuery("UPDATE StateContentEntity SET isactive = 0 WHERE stateContentStateUid = :stateUid AND stateContentKey = :stateContentKey")
    public abstract void setInActiveState(String stateContentKey, long stateUid);
}
