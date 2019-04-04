package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.StateEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
@UmRepository
public abstract class StateDao implements SyncableDao<StateEntity, StateDao> {

    @UmQuery("SELECT * FROM StateEntity WHERE stateId = :id LIMIT 1")
    public abstract StateEntity findByStateId(String id);

}
