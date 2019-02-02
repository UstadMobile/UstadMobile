package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class ContentEntryFileStatusDao implements BaseDao<ContentEntryFileStatus> {

    @UmQuery("SELECT * FROM ContentEntryFileStatus WHERE " +
            "filePath LIKE '%/khan/en/%'")
    public abstract List<ContentEntryFileStatus> findKhan();

    @UmQuery("UPDATE ContentEntryFileStatus SET filePath = :path WHERE cefsUid = :cefsUid")
    public abstract void updateKhanFilePath(int cefsUid, String path);
}
