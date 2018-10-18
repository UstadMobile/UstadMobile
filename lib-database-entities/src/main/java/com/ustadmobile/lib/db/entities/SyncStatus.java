package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class SyncStatus {

    @UmPrimaryKey
    private int tableId;

    private long nextMasterChangeNum;

    private long nextLocalChangeSeqNum;

    private long syncedToMasterChangeNum;

    private long syncedToLocalChangeSeqNum;

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public long getNextMasterChangeNum() {
        return nextMasterChangeNum;
    }

    public void setNextMasterChangeNum(long nextMasterChangeNum) {
        this.nextMasterChangeNum = nextMasterChangeNum;
    }

    public long getNextLocalChangeSeqNum() {
        return nextLocalChangeSeqNum;
    }

    public void setNextLocalChangeSeqNum(long nextLocalChangeSeqNum) {
        this.nextLocalChangeSeqNum = nextLocalChangeSeqNum;
    }

    public long getSyncedToMasterChangeNum() {
        return syncedToMasterChangeNum;
    }

    public void setSyncedToMasterChangeNum(long syncedToMasterChangeNum) {
        this.syncedToMasterChangeNum = syncedToMasterChangeNum;
    }

    public long getSyncedToLocalChangeSeqNum() {
        return syncedToLocalChangeSeqNum;
    }

    public void setSyncedToLocalChangeSeqNum(long syncedToLocalChangeSeqNum) {
        this.syncedToLocalChangeSeqNum = syncedToLocalChangeSeqNum;
    }
}
