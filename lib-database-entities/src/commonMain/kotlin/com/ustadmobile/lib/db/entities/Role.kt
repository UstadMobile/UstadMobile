package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 45)
class Role {


    @UmPrimaryKey(autoGenerateSyncable = true)
    var roleUid: Long = 0

    var roleName: String? = null

    @UmSyncMasterChangeSeqNum
    var roleMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var roleLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var roleLastChangedBy: Int = 0

    //bit flags made of up PERMISSION_ constants
    var rolePermissions: Long = 0

    //active
    var isRoleActive: Boolean = false

    constructor() {
        this.isRoleActive = true
    }

    constructor(roleName: String, rolePermissions: Long) {
        this.roleName = roleName
        this.rolePermissions = rolePermissions
        this.isRoleActive = true
    }

    companion object {

        const val ROLE_NAME_TEACHER = "teacher"
        const val ROLE_NAME_OFFICER = "officer"
        const val ROLE_NAME_MNE = "mne"
        const val ROLE_NAME_SEL = "sel"
        const val ROLE_NAME_SITE_STAFF = "sitestaff"

        const val PERMISSION_CLAZZ_SELECT: Long = 1

        const val PERMISSION_CLAZZ_INSERT: Long = 2

        const val PERMISSION_CLAZZ_UPDATE: Long = 4

        const val PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT: Long = 8

        const val PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT: Long = 16

        const val PERMISSION_SEL_QUESTION_RESPONSE_INSERT: Long = 32

        const val PERMISSION_PERSON_SELECT: Long = 64

        const val PERMISSION_PERSON_INSERT: Long = 128

        const val PERMISSION_PERSON_UPDATE: Long = 256

        const val PERMISSION_CLAZZ_ADD_TEACHER: Long = 512

        const val PERMISSION_CLAZZ_ADD_STUDENT: Long = 1024

        const val PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT: Long = 2048

        const val PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE: Long = 4096

        const val PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE: Long = 8192

        const val PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT: Long = 16384

        const val PERMISSION_SEL_QUESTION_RESPONSE_SELECT: Long = 32768

        const val PERMISSION_SEL_QUESTION_RESPONSE_UPDATE: Long = 65536

        const val PERMISSION_SEL_QUESTION_SELECT: Long = 131072

        const val PERMISSION_SEL_QUESTION_INSERT: Long = 262144

        const val PERMISSION_SEL_QUESTION_UPDATE: Long = 524288

        const val PERMISSION_PERSON_PICTURE_SELECT: Long = 1048576

        const val PERMISSION_PERSON_PICTURE_INSERT: Long = 2097152

        const val PERMISSION_PERSON_PICTURE_UPDATE: Long = 4194304

        /**
         * Permission to view reports. In reality, this is really just a UI permission, and does not
         * affect access to the underlying data.
         */
        val PERMISSION_REPORTS_VIEW: Long = 8388608
    }
}
