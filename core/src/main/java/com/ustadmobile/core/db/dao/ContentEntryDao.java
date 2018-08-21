package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntry;

@UmDao
public abstract class ContentEntryDao implements BaseDao<ContentEntry> {

    @Override
    @UmInsert
    public abstract long insert(ContentEntry entity);

    @Override
    @UmInsert
    public abstract void insertAsync(ContentEntry entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * From ContentEntry WHERE contentEntryUid = :uid")
    public abstract ContentEntry findByUid(long uid);
}
