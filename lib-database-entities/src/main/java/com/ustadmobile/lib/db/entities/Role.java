package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 45)
public class Role {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long roleUid;

    private String roleName;

    @UmSyncMasterChangeSeqNum
    private long roleMasterCsn;

    @UmSyncLocalChangeSeqNum
    private long roleLocalCsn;

    @UmSyncLastChangedBy
    private int roleLastChangedBy;

    //bit flags made of up PERMISSION_ constants
    private long rolePermissions;

    public static final long PERMISSION_CLAZZ_SELECT = 1;

    public static final long PERMISSION_CLAZZ_INSERT = 2;

    public static final long PERMISSION_CLAZZ_UPDATE = 4;

    public static final long PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT = 8;

    public static final long PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT = 16;

    public static final long PERMISSION_SEL_QUESTION_RESPONSE_INSERT = 32;

    public static final long PERMISSION_PERSON_SELECT = 64;

    public static final long PERMISSION_PERSON_INSERT = 128;

    public static final long PERMISSION_PERSON_UPDATE = 256;

    public static final long PERMISSION_CLAZZ_ADD_TEACHER = 512;

    public static final long PERMISSION_CLAZZ_ADD_STUDENT = 1024;

    public static final long PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT = 2048;

    public static final long PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE = 4096;

    public static final long PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE = 8192;

    public static final long PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT = 16384;

    public static final long PERMISSION_SEL_QUESTION_RESPONSE_SELECT = 32768;

    public static final long PERMISSION_SEL_QUESTION_RESPONSE_UPDATE = 65536;

    public static final long PERMISSION_SEL_QUESTION_SELECT = 131072;

    public static final long PERMISSION_SEL_QUESTION_INSERT = 262144;

    public static final long PERMISSION_SEL_QUESTION_UPDATE = 524288;

    public static final long PERMISSION_PERSON_PICTURE_SELECT = 1048576;

    public static final long PERMISSION_PERSON_PICTURE_INSERT = 2097152;

    public static final long PERMISSION_PERSON_PICTURE_UPDATE = 4194304;


    public Role() {

    }

    public Role(String roleName, long rolePermissions) {
        this.roleName = roleName;
        this.rolePermissions = rolePermissions;
    }

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
