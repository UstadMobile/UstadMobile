package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Role.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class Role() {

    @PrimaryKey(autoGenerate = true)
    var roleUid: Long = 0

    var roleName: String? = null

    var roleActive: Boolean = true

    @MasterChangeSeqNum
    var roleMasterCsn: Long = 0

    @LocalChangeSeqNum
    var roleLocalCsn: Long = 0

    @LastChangedBy
    var roleLastChangedBy: Int = 0

    //bit flags made of up PERMISSION_ constants
    var rolePermissions: Long = 0

    constructor(roleName: String, rolePermissions: Long):this() {
        this.roleName = roleName
        this.rolePermissions = rolePermissions
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Role

        if (roleUid != other.roleUid) return false
        if (roleName != other.roleName) return false
        if (roleActive != other.roleActive) return false
        if (roleMasterCsn != other.roleMasterCsn) return false
        if (roleLocalCsn != other.roleLocalCsn) return false
        if (roleLastChangedBy != other.roleLastChangedBy) return false
        if (rolePermissions != other.rolePermissions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roleUid.hashCode()
        result = 31 * result + (roleName?.hashCode() ?: 0)
        result = 31 * result + roleActive.hashCode()
        result = 31 * result + roleMasterCsn.hashCode()
        result = 31 * result + roleLocalCsn.hashCode()
        result = 31 * result + roleLastChangedBy
        result = 31 * result + rolePermissions.hashCode()
        return result
    }

    companion object {

        const val TABLE_ID = 45

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

        const val PERMISSION_CLAZZ_ASSIGNMENT_VIEW : Long = 8388608

        //There is no "insert" for CLAZZ_ASSIGNMENT as they are all tied to classes, so are considered updates
        const val PERMISSION_CLAZZ_ASSIGNMENT_UPDATE : Long = 16777216

        const val PERMISSION_CLAZZ_ASSIGNMENT_VIEWSTUDENTPROGRESS : Long= 33554432

        const val PERMISSION_CONTENT_SELECT : Long= 67108864

        const val PERMISSION_CONTENT_INSERT : Long= 134217728

        const val PERMISSION_CONTENT_UPDATE : Long= 268435456

        const val PERMISSION_SCHOOL_SELECT: Long = 536870912

        const val PERMISSION_SCHOOL_INSERT: Long = 1073741824

        const val PERMISSION_SCHOOL_UPDATE: Long = 2147483648L

        const val PERMISSION_PERSON_DELEGATE: Long = 4294967296L

        //Permission to actually open and enter the class (eg. available to accept members, not those with pending requests)
        const val PERMISSION_CLAZZ_OPEN: Long = 8589934592L

        const val PERMISSION_ROLE_SELECT : Long = 17179869184L

        const val PERMISSION_ROLE_INSERT: Long = 34359738368L

        const val PERMISSION_RESET_PASSWORD: Long = 68719476736L

        const val PERMISSION_SCHOOL_ADD_STAFF: Long = 137438953472L

        const val PERMISSION_SCHOOL_ADD_STUDENT: Long = 274877906944L

        /**
         * Permission to view the learner records of a person (e.g. Xapi statements, progress, etc)
         */
        const val PERMISSION_PERSON_LEARNINGRECORD_SELECT: Long = 549755813888L

        const val PERMISSION_PERSON_LEARNINGRECORD_INSERT: Long = 1099511627776L

        const val PERMISSION_PERSON_LEARNINGRECORD_UPDATE: Long = 2199023255552L

        //Predefined roles that are added by the system
        const val ROLE_CLAZZ_TEACHER_NAME = "Teacher"

        const val ROLE_CLAZZ_TEACHER_UID = 1001

        const val ROLE_CLAZZ_TEACHER_PERMISSIONS_DEFAULT: Long =
                PERMISSION_CLAZZ_SELECT or
                PERMISSION_CLAZZ_UPDATE or
                PERMISSION_CLAZZ_OPEN or
                PERMISSION_CLAZZ_ADD_STUDENT or
                PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or
                PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT or
                PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE or
                PERMISSION_PERSON_SELECT or
                PERMISSION_PERSON_UPDATE or
                PERMISSION_PERSON_INSERT or
                PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or
                PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT or
                PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE or
                PERMISSION_CLAZZ_ASSIGNMENT_VIEW or
                PERMISSION_CLAZZ_ASSIGNMENT_UPDATE


        const val ROLE_CLAZZ_STUDENT_NAME = "Class Student"

        const val ROLE_CLAZZ_STUDENT_UID = 1000

        const val ROLE_CLAZZ_STUDENT_PERMISSIONS_DEFAULT: Long =
                PERMISSION_CLAZZ_SELECT or
                PERMISSION_CLAZZ_OPEN or
                PERMISSION_PERSON_SELECT or
                PERMISSION_CLAZZ_ASSIGNMENT_VIEW

        const val ROLE_CLAZZ_STUDENT_PENDING_NAME = "Student Pending"

        const val ROLE_CLAZZ_STUDENT_PENDING_UID = 1002

        const val ROLE_CLAZZ_STUDENT_PENDING_PERMISSION_DEFAULT: Long = PERMISSION_CLAZZ_SELECT

        const val ROLE_SCHOOL_STUDENT_UID = 1003

        const val ROLE_SCHOOL_STUDENT_NAME = "School Student"

        const val ROLE_SCHOOL_STUDENT_PERMISSION_DEFAULT: Long = PERMISSION_SCHOOL_SELECT

        const val ROLE_SCHOOL_STAFF_UID = 1004

        const val ROLE_SCHOOL_STAFF_NAME = "School Staff"

        /**
         * Default permissions for a staff member at school
         */
        const val ROLE_SCHOOL_STAFF_PERMISSIONS_DEFAULT: Long = PERMISSION_CLAZZ_SELECT or
                PERMISSION_CLAZZ_UPDATE or
                PERMISSION_CLAZZ_OPEN or
                PERMISSION_CLAZZ_ADD_STUDENT or
                PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or
                PERMISSION_PERSON_SELECT or
                PERMISSION_PERSON_UPDATE or
                PERMISSION_PERSON_INSERT or
                PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or
                PERMISSION_CLAZZ_ASSIGNMENT_VIEW or
                PERMISSION_SCHOOL_SELECT or
                PERMISSION_SCHOOL_ADD_STUDENT


        const val ROLE_SCHOOL_STUDENT_PENDING_UID = 1005

        const val ROLE_SCHOOL_STUDENT_PENDING_NAME = "School Student Pending"

        const val ROLE_SCHOOL_STUDENT_PENDING_PERMISSION_DEFAULT = PERMISSION_SCHOOL_SELECT


        const val ROLE_PRINCIPAL_UID = 1006

        const val ROLE_PRINCIPAL_NAME = "Principal"

        //All permissionss so far
        const val ROLE_PRINCIPAL_PERMISSIONS_DEFAULT : Long =
                PERMISSION_CLAZZ_SELECT or
                        PERMISSION_CLAZZ_INSERT or
                        PERMISSION_CLAZZ_UPDATE or
                        PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT or
                        PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT or
                        PERMISSION_SEL_QUESTION_RESPONSE_INSERT or
                        PERMISSION_PERSON_SELECT or
                        PERMISSION_PERSON_INSERT or
                        PERMISSION_PERSON_UPDATE or
                        PERMISSION_CLAZZ_ADD_TEACHER or
                        PERMISSION_CLAZZ_ADD_STUDENT or
                        PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT or
                        PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE or
                        PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE or
                        PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT or
                        PERMISSION_SEL_QUESTION_RESPONSE_SELECT or
                        PERMISSION_SEL_QUESTION_RESPONSE_UPDATE or
                        PERMISSION_SEL_QUESTION_SELECT or
                        PERMISSION_SEL_QUESTION_INSERT or
                        PERMISSION_SEL_QUESTION_UPDATE or
                        PERMISSION_PERSON_PICTURE_SELECT or
                        PERMISSION_PERSON_PICTURE_INSERT or
                        PERMISSION_PERSON_PICTURE_UPDATE or
                        PERMISSION_CLAZZ_ASSIGNMENT_VIEW  or
                        PERMISSION_CLAZZ_ASSIGNMENT_UPDATE  or
                        PERMISSION_CLAZZ_ASSIGNMENT_VIEWSTUDENTPROGRESS or
                        PERMISSION_CONTENT_SELECT or
                        PERMISSION_CONTENT_INSERT or
                        PERMISSION_CONTENT_UPDATE or
                        PERMISSION_SCHOOL_SELECT or
                        PERMISSION_SCHOOL_INSERT or
                        PERMISSION_SCHOOL_UPDATE or
                        PERMISSION_PERSON_DELEGATE or
                        PERMISSION_CLAZZ_OPEN or
                        PERMISSION_ROLE_SELECT  or
                        PERMISSION_ROLE_INSERT or
                        PERMISSION_RESET_PASSWORD or
                        PERMISSION_SCHOOL_ADD_STAFF or
                        PERMISSION_SCHOOL_ADD_STUDENT

    }
}
