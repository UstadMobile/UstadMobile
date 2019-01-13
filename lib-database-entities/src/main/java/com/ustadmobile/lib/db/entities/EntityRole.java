package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 47)
public class EntityRole {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long erUid;

    @UmSyncMasterChangeSeqNum
    private long erMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long erLocalCsn;

    @UmSyncLastChangedBy
    private int erLastChangedBy;

    @UmIndexField
    private int erTableId;

    @UmIndexField
    private long erEntityUid;

    @UmIndexField
    private long erGroupUid;

    @UmIndexField
    private long erRoleUid;

    public EntityRole() {

    }

    public EntityRole(int erTableId, long erEntityUid, long erGroupUid, long erRoleUid) {
        this.erTableId = erTableId;
        this.erEntityUid = erEntityUid;
        this.erGroupUid = erGroupUid;
        this.erRoleUid = erRoleUid;
    }


    public long getErUid() {
        return erUid;
    }

    public void setErUid(long erUid) {
        this.erUid = erUid;
    }

    public int getErTableId() {
        return erTableId;
    }

    public void setErTableId(int erTableId) {
        this.erTableId = erTableId;
    }

    public long getErEntityUid() {
        return erEntityUid;
    }

    public void setErEntityUid(long erEntityUid) {
        this.erEntityUid = erEntityUid;
    }

    public long getErGroupUid() {
        return erGroupUid;
    }

    public void setErGroupUid(long erGroupUid) {
        this.erGroupUid = erGroupUid;
    }

    public long getErRoleUid() {
        return erRoleUid;
    }

    public void setErRoleUid(long erRoleUid) {
        this.erRoleUid = erRoleUid;
    }

    public long getErMasterCsn() {
        return erMasterCsn;
    }

    public void setErMasterCsn(long erMasterCsn) {
        this.erMasterCsn = erMasterCsn;
    }

    public long getErLocalCsn() {
        return erLocalCsn;
    }

    public void setErLocalCsn(long erLocalCsn) {
        this.erLocalCsn = erLocalCsn;
    }

    public int getErLastChangedBy() {
        return erLastChangedBy;
    }

    public void setErLastChangedBy(int erLastChangedBy) {
        this.erLastChangedBy = erLastChangedBy;
    }

}
