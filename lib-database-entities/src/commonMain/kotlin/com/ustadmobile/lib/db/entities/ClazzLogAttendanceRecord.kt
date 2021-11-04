package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.FROM_CLAZZLOGATTENDANCERECORD_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.FROM_SCOPEDGRANT_TO_CLAZZLOGATTENDANCERECORD_JOIN_ON_CLAUSE
import kotlinx.serialization.Serializable

/*
@SyncableEntity(tableId = ClazzLogAttendanceRecord.TABLE_ID,
    notifyOnUpdate = [
        """
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId 
          FROM ChangeLog
            JOIN ClazzLogAttendanceRecord 
                 ON ChangeLog.chTableId = ${ClazzLogAttendanceRecord.TABLE_ID} 
                    AND ChangeLog.chEntityPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
            JOIN ScopedGrant 
                 ON $FROM_CLAZZLOGATTENDANCERECORD_TO_SCOPEDGRANT_JOIN_ON_CLAUSE
                    AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT}) > 0
            JOIN PersonGroupMember 
                 ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
            JOIN UserSession
                 ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                    AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """
    ],
    syncFindAllQuery = """
            SELECT ClazzLogAttendanceRecord.*
              FROM UserSession
                   JOIN PersonGroupMember 
                        ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                   JOIN ScopedGrant 
                        ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                            AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT}) > 0
                   JOIN ClazzLogAttendanceRecord 
                        ON $FROM_SCOPEDGRANT_TO_CLAZZLOGATTENDANCERECORD_JOIN_ON_CLAUSE
             WHERE UserSession.usClientNodeId = :clientId
                   AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """)

 */
@Entity
@Serializable
open class ClazzLogAttendanceRecord {

    @PrimaryKey(autoGenerate = true)
    var clazzLogAttendanceRecordUid: Long = 0

    var clazzLogAttendanceRecordClazzLogUid: Long = 0

    var clazzLogAttendanceRecordPersonUid: Long = 0

    var attendanceStatus: Int = 0

    @MasterChangeSeqNum
    var clazzLogAttendanceRecordMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLogAttendanceRecordLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLogAttendanceRecordLastChangedBy: Int = 0

    @LastChangedTime
    var clazzLogAttendanceRecordLastChangedTime: Long = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzLogAttendanceRecord

        if (clazzLogAttendanceRecordUid != other.clazzLogAttendanceRecordUid) return false
        if (clazzLogAttendanceRecordClazzLogUid != other.clazzLogAttendanceRecordClazzLogUid) return false
        if (clazzLogAttendanceRecordPersonUid != other.clazzLogAttendanceRecordPersonUid) return false
        if (attendanceStatus != other.attendanceStatus) return false
        if (clazzLogAttendanceRecordMasterChangeSeqNum != other.clazzLogAttendanceRecordMasterChangeSeqNum) return false
        if (clazzLogAttendanceRecordLocalChangeSeqNum != other.clazzLogAttendanceRecordLocalChangeSeqNum) return false
        if (clazzLogAttendanceRecordLastChangedBy != other.clazzLogAttendanceRecordLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzLogAttendanceRecordUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordClazzLogUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordPersonUid.hashCode()
        result = 31 * result + attendanceStatus
        result = 31 * result + clazzLogAttendanceRecordMasterChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLocalChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLastChangedBy
        return result
    }


    companion object {

        const val FROM_CLAZZLOGATTENDANCERECORD_TO_SCOPEDGRANT_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
             OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                AND ScopedGrant.sgEntityUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid)
             OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                AND ScopedGrant.sgEntityUid = (
                    SELECT clazzLogClazzUid 
                      FROM ClazzLog
                     WHERE clazzLogUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid))
             OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                AND ScopedGrant.sgEntityUid = (
                    SELECT clazzSchoolUid
                      FROM Clazz
                     WHERE clazzUid = (
                            SELECT clazzLogClazzUid 
                              FROM ClazzLog
                             WHERE clazzLogUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid)))
                     
                     )
            
        """

        const val FROM_SCOPEDGRANT_TO_CLAZZLOGATTENDANCERECORD_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
             OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                AND ScopedGrant.sgEntityUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordPersonUid)
             OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                AND ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid IN (
                    SELECT clazzLogUid 
                      FROM ClazzLog
                     WHERE clazzLogClazzUid = ScopedGrant.sgEntityUid))
             OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                AND ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid IN (
                     SELECT clazzLogUid
                       FROM ClazzLog
                      WHERE clazzLogClazzUid IN (
                            SELECT clazzUid
                              FROM Clazz
                             WHERE clazzSchoolUid = ScopedGrant.sgEntityUid)))
            )         
        """


        const val TABLE_ID = 15

        const val STATUS_ATTENDED = 1

        const val STATUS_ABSENT = 2

        const val STATUS_PARTIAL = 4
    }


}
