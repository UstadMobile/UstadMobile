package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntry.TABLE_ID;

/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@UmEntity(tableId = TABLE_ID)
public class ContentEntryParentChildJoin {

    public static final int TABLE_ID = 7;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long cepcjUid;

    private long cepcjChildContentEntryUid;

    private long cepcjParentContentEntryUid;

    private int childIndex;

    @UmSyncLocalChangeSeqNum
    private long cepcjLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long cepcjMasterChangeSeqNum;

    public long getCepcjUid() {
        return cepcjUid;
    }

    public void setCepcjUid(long cepcjUid) {
        this.cepcjUid = cepcjUid;
    }

    public long getCepcjChildContentEntryUid() {
        return cepcjChildContentEntryUid;
    }

    public void setCepcjChildContentEntryUid(long cepcjChildContentEntryUid) {
        this.cepcjChildContentEntryUid = cepcjChildContentEntryUid;
    }

    public long getCepcjParentContentEntryUid() {
        return cepcjParentContentEntryUid;
    }

    public void setCepcjParentContentEntryUid(long cepcjParentContentEntryUid) {
        this.cepcjParentContentEntryUid = cepcjParentContentEntryUid;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }

    public long getCepcjLocalChangeSeqNum() {
        return cepcjLocalChangeSeqNum;
    }

    public void setCepcjLocalChangeSeqNum(long cepcjLocalChangeSeqNum) {
        this.cepcjLocalChangeSeqNum = cepcjLocalChangeSeqNum;
    }

    public long getCepcjMasterChangeSeqNum() {
        return cepcjMasterChangeSeqNum;
    }

    public void setCepcjMasterChangeSeqNum(long cepcjMasterChangeSeqNum) {
        this.cepcjMasterChangeSeqNum = cepcjMasterChangeSeqNum;
    }
}
