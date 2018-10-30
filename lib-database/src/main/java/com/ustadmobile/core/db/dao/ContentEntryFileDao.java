package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryFile;

import java.util.List;

@UmDao
public abstract class ContentEntryFileDao implements BaseDao<ContentEntryFile> {

    @UmQuery("Select ContentEntryFile.* FROM ContentEntryFile LEFT JOIN ContentEntryContentEntryFileJoin " +
                    "ON ContentEntryFile.contentEntryFileUid = ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid " +
                    "WHERE ContentEntryContentEntryFileJoin.cecefjContentEntryUid = :contentEntryUid")
    public abstract void findFilesByContentEntryUid(long contentEntryUid, UmCallback<List<ContentEntryFile>> callback);

}
