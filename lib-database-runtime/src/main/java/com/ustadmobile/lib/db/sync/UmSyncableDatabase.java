package com.ustadmobile.lib.db.sync;


import com.ustadmobile.lib.db.sync.dao.SyncStatusDao;
import com.ustadmobile.lib.db.sync.dao.SyncablePrimaryKeyDao;

public interface UmSyncableDatabase {

    SyncStatusDao getSyncStatusDao();

    SyncablePrimaryKeyDao getSyncablePrimaryKeyDao();

    boolean isMaster();

    void setMaster(boolean master);

    int getDeviceBits();

    void invalidateDeviceBits();

}
