package com.ustadmobile.lib.db.sync;


import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;

public interface UmSyncableDatabase {

    SyncStatusDao getSyncStatusDao();

    boolean isMaster();

}
