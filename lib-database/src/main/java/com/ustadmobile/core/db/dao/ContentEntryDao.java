package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntry;

@UmDao
public abstract class ContentEntryDao implements BaseDao<ContentEntry> {

    @UmQuery("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl")
    public abstract ContentEntry findBySourceUrl(String sourceUrl);

    @UmUpdate
    public abstract int updateContentEntry(ContentEntry entry);

}
