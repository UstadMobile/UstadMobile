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
            incrementLocalChangeSeqNum(tableId, numEntries);
        }else {
            insert(new SyncStatus(tableId));
        }

        return seqNum + 1;
    }

    @UmQuery("SELECT localChangeSeqNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getNextLocalChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET localChangeSeqNum = localChangeSeqNum + :increment WHERE tableId = :tableId")
    public abstract void incrementLocalChangeSeqNum(int tableId, int increment);

    public long getAndIncrementNextMasterChangeSeqNum(int tableId, int numEntries) {
        long seqNum = getMasterChangeSeqNum(tableId);
        if(seqNum >= 1){
            incrementMasterChangeSeqNum(tableId, numEntries);
        }else {
            insert(new SyncStatus(tableId));
        }

        return seqNum + 1;
    }

    @UmQuery("SELECT masterChangeSeqNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getMasterChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET masterChangeSeqNum = masterChangeSeqNum + :increment WHERE tableId = :tableId")
    public abstract void incrementMasterChangeSeqNum(int tableId, int increment);

    @UmQuery("UPDATE SyncStatus SET syncedToLocalChangeSeqNum = :syncedToLocalChangeSeqNum, syncedToMasterChangeNum = :syncdToMasterChangeNum WHERE tableId = :tableId")
    public abstract void updateSyncedToChangeSeqNums(int tableId, long syncedToLocalChangeSeqNum, long syncdToMasterChangeNum);

}
