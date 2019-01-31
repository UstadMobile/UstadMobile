package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class ContentEntryStatusDao implements BaseDao<ContentEntryStatus> {

    public void refresh() {

    }

    @UmQuery("SELECT * FROM ContentEntryStatus WHERE invalidated")
    public abstract List<ContentEntryStatus> findAllInvalidated();

    protected abstract void updateEntries(List<Integer> uidsToUpdate);


}
