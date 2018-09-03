package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntry;

import java.util.List;

@UmDao
public abstract class ContentEntryDao {
    @UmInsert(onConflict = UmOnConflictStrategy.IGNORE)
    public abstract long [] insert(List<ContentEntry> contentEntries);

    @UmQuery("SELECT * FROM ContentEntry WHERE contentEntryUid=:entryUuid")
    public abstract ContentEntry findByEntryId(long entryUuid);
}
