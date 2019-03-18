package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;


/**
 * Deprecated: this si being replaced with Container which support de-duplicating entries
 */
@UmDao
@Deprecated
public abstract class ContentEntryFileStatusDao implements BaseDao<ContentEntryFileStatus> {


    @UmQuery("DELETE FROM ContentEntryFileStatus WHERE cefsUid = :statusUid")
    public abstract void deleteByUid(int statusUid);

}
