package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.XObjectEntity;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
@UmRepository
public abstract class XObjectDao implements SyncableDao<XObjectEntity, XObjectDao> {


    @UmQuery("SELECT * from XObjectEntity WHERE objectId = :id")
    public abstract XObjectEntity findByObjectId(String id);
}
