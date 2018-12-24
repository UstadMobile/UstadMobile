package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 46)
public class RolePermission {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private int rolePermissionUid;

    private int rolePermissionRoleUid;

    private int rolePermissionPermissionId;

    @UmSyncMasterChangeSeqNum
    private long rolePermissionMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long rolePermissionLocalCsn;

    @UmSyncLastChangedBy
    private int rolePermissionLastChangedBy;

    public int getRolePermissionUid() {
        return rolePermissionUid;
    }

    public void setRolePermissionUid(int rolePermissionUid) {
        this.rolePermissionUid = rolePermissionUid;
    }

    public int getRolePermissionRoleUid() {
        return rolePermissionRoleUid;
    }

    public void setRolePermissionRoleUid(int rolePermissionRoleUid) {
        this.rolePermissionRoleUid = rolePermissionRoleUid;
    }

    public int getRolePermissionPermissionId() {
        return rolePermissionPermissionId;
    }

    public void setRolePermissionPermissionId(int rolePermissionPermissionId) {
        this.rolePermissionPermissionId = rolePermissionPermissionId;
    }

    public long getRolePermissionMasterCsn() {
        return rolePermissionMasterCsn;
    }

    public void setRolePermissionMasterCsn(long rolePermissionMasterCsn) {
        this.rolePermissionMasterCsn = rolePermissionMasterCsn;
    }

    public long getRolePermissionLocalCsn() {
        return rolePermissionLocalCsn;
    }

    public void setRolePermissionLocalCsn(long rolePermissionLocalCsn) {
        this.rolePermissionLocalCsn = rolePermissionLocalCsn;
    }

    public int getRolePermissionLastChangedBy() {
        return rolePermissionLastChangedBy;
    }

    public void setRolePermissionLastChangedBy(int rolePermissionLastChangedBy) {
        this.rolePermissionLastChangedBy = rolePermissionLastChangedBy;
    }
}
