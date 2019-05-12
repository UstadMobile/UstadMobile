package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin.TABLE_ID;


/**
 * Join entity to link ContentEntry many:many with ContentCategory
 */
@UmEntity(tableId = TABLE_ID)
public class ContentEntryContentCategoryJoin {

    public static final int TABLE_ID = 3;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long ceccjUid;

    //TODO: Migration
    @UmIndexField
    private long ceccjContentEntryUid;

    private long ceccjContentCategoryUid;

    @UmSyncLocalChangeSeqNum
    private long ceccjLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long ceccjMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int ceccjLastChangedBy;

    public long getCeccjUid() {
        return ceccjUid;
    }

    public void setCeccjUid(long ceccjUid) {
        this.ceccjUid = ceccjUid;
    }

    public long getCeccjContentEntryUid() {
        return ceccjContentEntryUid;
    }

    public void setCeccjContentEntryUid(long ceccjContentEntryUid) {
        this.ceccjContentEntryUid = ceccjContentEntryUid;
    }

    public long getCeccjContentCategoryUid() {
        return ceccjContentCategoryUid;
    }

    public void setCeccjContentCategoryUid(long ceccjContentCategoryUid) {
        this.ceccjContentCategoryUid = ceccjContentCategoryUid;
    }

    public long getCeccjLocalChangeSeqNum() {
        return ceccjLocalChangeSeqNum;
    }

    public void setCeccjLocalChangeSeqNum(long ceccjLocalChangeSeqNum) {
        this.ceccjLocalChangeSeqNum = ceccjLocalChangeSeqNum;
    }

    public long getCeccjMasterChangeSeqNum() {
        return ceccjMasterChangeSeqNum;
    }

    public void setCeccjMasterChangeSeqNum(long ceccjMasterChangeSeqNum) {
        this.ceccjMasterChangeSeqNum = ceccjMasterChangeSeqNum;
    }

    public int getCeccjLastChangedBy() {
        return ceccjLastChangedBy;
    }

    public void setCeccjLastChangedBy(int ceccjLastChangedBy) {
        this.ceccjLastChangedBy = ceccjLastChangedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntryContentCategoryJoin that = (ContentEntryContentCategoryJoin) o;

        if (ceccjUid != that.ceccjUid) return false;
        if (ceccjContentEntryUid != that.ceccjContentEntryUid) return false;
        return ceccjContentCategoryUid == that.ceccjContentCategoryUid;
    }

    @Override
    public int hashCode() {
        int result = (int) (ceccjUid ^ (ceccjUid >>> 32));
        result = 31 * result + (int) (ceccjContentEntryUid ^ (ceccjContentEntryUid >>> 32));
        result = 31 * result + (int) (ceccjContentCategoryUid ^ (ceccjContentCategoryUid >>> 32));
        return result;
    }
}
