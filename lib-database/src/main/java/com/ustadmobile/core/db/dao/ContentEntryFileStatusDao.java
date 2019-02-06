package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

@UmDao
public abstract class ContentEntryFileStatusDao implements BaseDao<ContentEntryFileStatus> {

    @UmQuery("SELECT * FROM ContentEntryFileStatus WHERE cefsContentEntryFileUid = :cefsContentEntryFileUid")
    public abstract ContentEntryFileStatus findByContentEntryFileUid(long cefsContentEntryFileUid);

}
