package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndex;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin.TABLE_ID;


/**
 * ContentEntry child - parent join entity
 */
//short code = cepcj
@UmEntity(tableId = TABLE_ID, indices =
        {@UmIndex(name="parent_child", value =
                {"cepcjChildContentEntryUid",
                "cepcjParentContentEntryUid"})})
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

    @UmSyncLastChangedBy
    private int cepcjLastChangedBy;

    public ContentEntryParentChildJoin() {

    }

    public ContentEntryParentChildJoin(ContentEntry parentEntry, ContentEntry childEntry,
                                       int childIndex) {
        this.cepcjParentContentEntryUid = parentEntry.getContentEntryUid();
        this.cepcjChildContentEntryUid = childEntry.getContentEntryUid();
        this.childIndex = childIndex;
    }

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

    public int getCepcjLastChangedBy() {
        return cepcjLastChangedBy;
    }

    public void setCepcjLastChangedBy(int cepcjLastChangedBy) {
        this.cepcjLastChangedBy = cepcjLastChangedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntryParentChildJoin that = (ContentEntryParentChildJoin) o;

        if (cepcjUid != that.cepcjUid) return false;
        if (cepcjChildContentEntryUid != that.cepcjChildContentEntryUid) return false;
        if (cepcjParentContentEntryUid != that.cepcjParentContentEntryUid) return false;
        return childIndex == that.childIndex;
    }

    @Override
    public int hashCode() {
        int result = (int) (cepcjUid ^ (cepcjUid >>> 32));
        result = 31 * result + (int) (cepcjChildContentEntryUid ^ (cepcjChildContentEntryUid >>> 32));
        result = 31 * result + (int) (cepcjParentContentEntryUid ^ (cepcjParentContentEntryUid >>> 32));
        result = 31 * result + childIndex;
        return result;
    }
}
