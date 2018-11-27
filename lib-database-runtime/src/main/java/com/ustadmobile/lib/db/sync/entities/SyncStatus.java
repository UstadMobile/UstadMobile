package com.ustadmobile.lib.db.sync.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
@Entity
public class SyncStatus {

    @UmPrimaryKey
    @PrimaryKey
    private int tableId;

    private long masterChangeSeqNum;

    private long localChangeSeqNum;

    private long syncedToMasterChangeNum;

    private long syncedToLocalChangeSeqNum;

    public SyncStatus() {

    }

    public SyncStatus(int tableId) {
        this.tableId = tableId;
        this.masterChangeSeqNum = 1;
        this.localChangeSeqNum = 1;
    }


    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public long getMasterChangeSeqNum() {
        return masterChangeSeqNum;
    }

    public void setMasterChangeSeqNum(long masterChangeSeqNum) {
        this.masterChangeSeqNum = masterChangeSeqNum;
    }

    public long getLocalChangeSeqNum() {
        return localChangeSeqNum;
    }

    public void setLocalChangeSeqNum(long localChangeSeqNum) {
        this.localChangeSeqNum = localChangeSeqNum;
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
