package com.ustadmobile.lib.db.sync.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

@UmDao
public abstract class SyncStatusDao implements BaseDao<SyncStatus> {

    /**
     * Finder method that will auto-create the desired entity if it does not already exist
     *
     * @param tableId
     * @return
     */
    public SyncStatus getByUid(int tableId) {
        SyncStatus syncStatus = findByUid(tableId);
        if(syncStatus != null)
            return syncStatus;

        syncStatus = new SyncStatus(tableId);
        insert(syncStatus);
        return syncStatus;
    }

    @UmQuery("SELECT nextChangeSeqNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getNextChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET syncedToLocalChangeSeqNum = :syncedToLocalChangeSeqNum, syncedToMasterChangeNum = :syncdToMasterChangeNum WHERE tableId = :tableId")
    public abstract void updateSyncedToChangeSeqNums(int tableId, long syncedToLocalChangeSeqNum, long syncdToMasterChangeNum);

}
