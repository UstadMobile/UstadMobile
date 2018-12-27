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

    //bit flags made of up PERMISSION_ constants
    private long rolePermissions;

    public static final long PERMISSION_SELECT = 1;

    public static final long PERMISSION_INSERT = 2;

    public static final long PERMISSION_UPDATE = 4;

    public static final long PERMISSION_CLAZZ_RECORD_ATTENDANCE = 8;

    public static final long PERMISSION_CLAZZ_RECORD_ACTIVITY = 16;

    public static final long PERMISSION_CLAZZ_RECORD_SEL = 32;

    public static final long PERMISSION_CLAZZ_VIEW_ATTENDANCE = 64;

    public static final long PERMISSION_CLAZZ_VIEW_ACTIVITY = 128;

    public static final long PERMISSION_CLAZZ_VIEW_SEL = 256;

    public static final long PERMISSION_CLAZZ_VIEW_REPORTS = 512;

    
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

    public long getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(long rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
