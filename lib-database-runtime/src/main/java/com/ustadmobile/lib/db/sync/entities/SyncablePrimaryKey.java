package com.ustadmobile.lib.db.sync.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
@Entity
public class SyncablePrimaryKey {

    @UmPrimaryKey
    @PrimaryKey
    private int tableId;

    private int sequenceNumber;

    public SyncablePrimaryKey() {

    }

    public SyncablePrimaryKey(int tableId, int sequenceNumber) {
        this.tableId = tableId;
        this.sequenceNumber = sequenceNumber;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
