package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao
@UmRepository
public abstract class ContentEntryRelatedEntryJoinDao
        implements BaseDao<ContentEntryRelatedEntryJoin> { //SyncableDao<ContentEntryRelatedEntryJoin, ContentEntryRelatedEntryJoinDao> {

    @UmQuery("SELECT * from ContentEntryRelatedEntryJoin WHERE " +
            "cerejRelatedEntryUid = :contentEntryUid")
    public abstract ContentEntryRelatedEntryJoin findPrimaryByTranslation(long contentEntryUid);

    @UmUpdate
    public abstract void update(ContentEntryRelatedEntryJoin entity);
}
