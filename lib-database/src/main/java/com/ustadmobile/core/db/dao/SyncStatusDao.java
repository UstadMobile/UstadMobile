package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.SyncStatus;

@UmDao
public abstract class SyncStatusDao implements BaseDao<SyncStatus> {

    public long getAndIncrementNextLocalChangeSeqNum(int tableId, int numEntries) {
        long seqNum = getNextLocalChangeSeqNum(tableId);
        updateNextLocalChangeSeqNum(tableId, numEntries);
        return seqNum + 1;
    }

    @UmQuery("SELECT nextLocalChangeSeqNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getNextLocalChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET nextLocalChangeSeqNum = nextLocalChangeSeqNum + :increment WHERE tableId = :tableId")
    public abstract void updateNextLocalChangeSeqNum(int tableId, int increment);

    public long getAndIncrementNextMasterChangeSeqNum(int tableId, int numEntries) {
        long seqNum = getNextMasterChangeSeqNum(tableId);
        updateNextMasterChangeSeqNum(tableId, numEntries);
        return seqNum + 1;
    }

    @UmQuery("SELECT nextMasterChangeNum FROM SyncStatus WHERE tableId = :tableId")
    public abstract long getNextMasterChangeSeqNum(int tableId);

    @UmQuery("UPDATE SyncStatus SET nextMasterChangeNum = nextMasterChangeNum + :increment WHERE tableId = :tableId")
    public abstract void updateNextMasterChangeSeqNum(int tableId, int increment);

    @UmQuery("UPDATE SyncStatus SET syncedToLocalChangeSeqNum = :syncedToLocalChangeSeqNum, syncedToMasterChangeNum = :syncedToMasterChangeSeqNum WHERE tableId = :tableId")
    public abstract void updateSyncedToChangeSeqNums(int tableId, long syncedToLocalChangeSeqNum,
                                                     long syncedToMasterChangeSeqNum);


}
