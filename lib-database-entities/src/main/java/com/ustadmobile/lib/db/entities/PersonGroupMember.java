package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 44)
@Entity
public class PersonGroupMember {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    private long groupMemberUid;

    @UmIndexField
    @ColumnInfo(index = true)
    private long groupMemberPersonUid;

    @UmIndexField
    @ColumnInfo(index = true)
    private long groupMemberGroupUid;

    @UmSyncMasterChangeSeqNum
    private long groupMemberMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long groupMemberLocalCsn;

    @UmSyncLastChangedBy
    private int groupMemberLastChangedBy;

    public long getGroupMemberUid() {
        return groupMemberUid;
    }

    public void setGroupMemberUid(long groupMemberUid) {
        this.groupMemberUid = groupMemberUid;
    }

    public long getGroupMemberPersonUid() {
        return groupMemberPersonUid;
    }

    public void setGroupMemberPersonUid(long groupMemberPersonUid) {
        this.groupMemberPersonUid = groupMemberPersonUid;
    }

    public long getGroupMemberGroupUid() {
        return groupMemberGroupUid;
    }

    public void setGroupMemberGroupUid(long groupMemberGroupUid) {
        this.groupMemberGroupUid = groupMemberGroupUid;
    }

    public long getGroupMemberMasterCsn() {
        return groupMemberMasterCsn;
    }

    public void setGroupMemberMasterCsn(long groupMemberMasterCsn) {
        this.groupMemberMasterCsn = groupMemberMasterCsn;
    }

    public long getGroupMemberLocalCsn() {
        return groupMemberLocalCsn;
    }

    public void setGroupMemberLocalCsn(long groupMemberLocalCsn) {
        this.groupMemberLocalCsn = groupMemberLocalCsn;
    }

    public int getGroupMemberLastChangedBy() {
        return groupMemberLastChangedBy;
    }

    public void setGroupMemberLastChangedBy(int groupMemberLastChangedBy) {
        this.groupMemberLastChangedBy = groupMemberLastChangedBy;
    }
}
