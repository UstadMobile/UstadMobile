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

    @UmQuery("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :id AND isactive")
    public abstract List<StateContentEntity> findAllStateContentWithStateUid(long id);

    @UmQuery("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :stateUid AND stateContentKey = :key AND isactive")
    public abstract StateContentEntity findStateContentByKeyAndStateUid(String key, long stateUid);

    @UmQuery("UPDATE StateContentEntity SET isactive = :isActive WHERE stateContentUid = :stateUid")
    public abstract void setInActiveStateContentByKeyAndUid(boolean isActive, long stateUid);


}
