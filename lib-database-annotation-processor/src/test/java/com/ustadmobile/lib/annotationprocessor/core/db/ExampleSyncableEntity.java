package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 42)
public class ExampleSyncableEntity {

    public static final int TABLE_ID = 42;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long exampleSyncableUid;

    @UmSyncMasterChangeSeqNum
    private long masterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long localChangeSeqNum;

    @UmSyncLastChangedBy
    private int lastChangedBy;

    private String title;

    public ExampleSyncableEntity() {

    }

    public ExampleSyncableEntity(String title) {
        this.title = title;
    }

    public long getExampleSyncableUid() {
        return exampleSyncableUid;
    }

    public void setExampleSyncableUid(long exampleSyncableUid) {
        this.exampleSyncableUid = exampleSyncableUid;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLastChangedBy() {
        return lastChangedBy;
    }

    public void setLastChangedBy(int lastChangedBy) {
        this.lastChangedBy = lastChangedBy;
    }
}
