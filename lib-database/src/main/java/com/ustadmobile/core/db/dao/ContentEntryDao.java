package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;


@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ContentEntryDao implements SyncableDao<ContentEntry, ContentEntryDao> {
    @UmInsert
    public abstract long [] insert(List<ContentEntry> contentEntries);

    @UmQuery("SELECT * FROM ContentEntry WHERE contentEntryUid=:entryUuid")
    public abstract ContentEntry findByEntryId(long entryUuid);

    @UmQuery("DELETE FROM ContentEntry")
    public abstract void deleteAll();
}
