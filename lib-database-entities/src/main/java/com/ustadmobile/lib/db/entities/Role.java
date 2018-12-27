package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 45)
public class Role {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long roleUid;

    private String roleName;

    @UmSyncMasterChangeSeqNum
    private long roleMasterCsn;

    @UmSyncMasterChangeSeqNum
    private long roleLocalCsn;

    @UmSyncLastChangedBy
    private int roleLastChangedBy;

    public long getRoleUid() {
        return roleUid;
    }

    public void setRoleUid(long roleUid) {
        this.roleUid = roleUid;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getRoleMasterCsn() {
        return roleMasterCsn;
    }

    public void setRoleMasterCsn(long roleMasterCsn) {
        this.roleMasterCsn = roleMasterCsn;
    }

    public long getRoleLocalCsn() {
        return roleLocalCsn;
    }

    public void setRoleLocalCsn(long roleLocalCsn) {
        this.roleLocalCsn = roleLocalCsn;
    }

    public int getRoleLastChangedBy() {
        return roleLastChangedBy;
    }

    public void setRoleLastChangedBy(int roleLastChangedBy) {
        this.roleLastChangedBy = roleLastChangedBy;
    }
}
