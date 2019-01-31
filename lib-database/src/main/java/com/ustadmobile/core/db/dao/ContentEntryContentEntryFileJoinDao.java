package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;

import java.util.List;

import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryContentEntryFileJoinDao
        implements SyncableDao<ContentEntryContentEntryFileJoin, ContentEntryContentEntryFileJoinDao> {

    @UmQuery("SELECT * from ContentEntryContentEntryFileJoin WHERE " +
            "cecefjContentEntryUid = :parentEntryContentUid")
    public abstract List<ContentEntryContentEntryFileJoin> findChildByParentUUid(long parentEntryContentUid);

    @UmUpdate
    public abstract void update(ContentEntryContentEntryFileJoin entity);

}
