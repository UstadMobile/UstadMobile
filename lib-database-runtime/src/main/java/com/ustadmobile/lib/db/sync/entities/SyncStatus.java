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

    /**
     * Represents the next change sequence number
     */
    private long nextChangeSeqNum;

    /**
     * The master change sequence number that we have sync'd up to (inclusive).
     */
    private long syncedToMasterChangeNum;

    /**
     * The local change sequence number that we have sync'd up to (inclusive)
     */
    private long syncedToLocalChangeSeqNum;

    public SyncStatus() {

    }

    public SyncStatus(int tableId) {
        this.tableId = tableId;
    }


    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
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

    public long getNextChangeSeqNum() {
        return nextChangeSeqNum;
    }

    public void setNextChangeSeqNum(long nextChangeSeqNum) {
        this.nextChangeSeqNum = nextChangeSeqNum;
    }
}
