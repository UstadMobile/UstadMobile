package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
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

    private int erTableId;

    private long erEntityUid;

    private long erGroupUid;

    private long erRoleUid;

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
}
