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

    public long getAndIncrementNextLocalChangeSeqNum(int tableId, int numEntries) {
        long seqNum = getNextLocalChangeSeqNum(tableId);
        if(seqNum >= 1) {
            updateNextLocalChangeSeqNum(tableId, numEntries);
        }else {
            insert(new SyncStatus(tableId));
        }

        return seqNum + 1;
    }

    @UmQuery("SELECT nextLocalChangeSeqNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getNextLocalChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET nextLocalChangeSeqNum = nextLocalChangeSeqNum + :increment WHERE tableId = :tableId")
    public abstract void updateNextLocalChangeSeqNum(int tableId, int increment);

    public long getAndIncrementNextMasterChangeSeqNum(int tableId, int numEntries) {
        long seqNum = getNextMasterChangeSeqNum(tableId);
        if(seqNum >= 1){
            updateNextMasterChangeSeqNum(tableId, numEntries);
        }else {
            insert(new SyncStatus(tableId));
        }

        return seqNum + 1;
    }

    @UmQuery("SELECT nextMasterChangeNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getNextMasterChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET nextMasterChangeNum = nextMasterChangeNum + :increment WHERE tableId = :tableId")
    public abstract void updateNextMasterChangeSeqNum(int tableId, int increment);

    @UmQuery("UPDATE SyncStatus SET syncedToLocalChangeSeqNum = :syncedToLocalChangeSeqNum, syncedToMasterChangeNum = :syncdToMasterChangeNum WHERE tableId = :tableId")
    public abstract void updateSyncedToChangeSeqNums(int tableId, long syncedToLocalChangeSeqNum, long syncdToMasterChangeNum);

}
