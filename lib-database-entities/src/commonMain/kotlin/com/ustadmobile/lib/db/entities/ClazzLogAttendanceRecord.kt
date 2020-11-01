package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = ClazzLogAttendanceRecord.TABLE_ID,
    notifyOnUpdate = [
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzLogAttendanceRecord.TABLE_ID} AS tableId FROM 
            ChangeLog
            JOIN ClazzLogAttendanceRecord ON ChangeLog.chTableId = ${ClazzLogAttendanceRecord.TABLE_ID} AND ChangeLog.chEntityPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
            JOIN ClazzMember ON ClazzMember.clazzMemberUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid 
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
        """
    ],
    syncFindAllQuery = """
            SELECT ClazzLogAttendanceRecord.* FROM
            ClazzLogAttendanceRecord
            JOIN ClazzMember ON ClazzMember.clazzMemberUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid 
            JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid
            JOIN Person Person_With_Perm ON Person_With_Perm.personUid IN 
                ( ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT1} 0 ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT2} ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT} ${Person.ENTITY_PERSONS_WITH_PERMISSION_PT4} )
            JOIN DeviceSession ON DeviceSession.dsPersonUid = Person_With_Perm.personUid
            WHERE DeviceSession.dsDeviceId = :clientId
        """)
@Entity
@Serializable
open class ClazzLogAttendanceRecord() {

    @PrimaryKey(autoGenerate = true)
    var clazzLogAttendanceRecordUid: Long = 0

    var clazzLogAttendanceRecordClazzLogUid: Long = 0

    var clazzLogAttendanceRecordClazzMemberUid: Long = 0

    var attendanceStatus: Int = 0

    @MasterChangeSeqNum
    var clazzLogAttendanceRecordMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLogAttendanceRecordLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLogAttendanceRecordLastChangedBy: Int = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzLogAttendanceRecord

        if (clazzLogAttendanceRecordUid != other.clazzLogAttendanceRecordUid) return false
        if (clazzLogAttendanceRecordClazzLogUid != other.clazzLogAttendanceRecordClazzLogUid) return false
        if (clazzLogAttendanceRecordClazzMemberUid != other.clazzLogAttendanceRecordClazzMemberUid) return false
        if (attendanceStatus != other.attendanceStatus) return false
        if (clazzLogAttendanceRecordMasterChangeSeqNum != other.clazzLogAttendanceRecordMasterChangeSeqNum) return false
        if (clazzLogAttendanceRecordLocalChangeSeqNum != other.clazzLogAttendanceRecordLocalChangeSeqNum) return false
        if (clazzLogAttendanceRecordLastChangedBy != other.clazzLogAttendanceRecordLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzLogAttendanceRecordUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordClazzLogUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordClazzMemberUid.hashCode()
        result = 31 * result + attendanceStatus
        result = 31 * result + clazzLogAttendanceRecordMasterChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLocalChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLastChangedBy
        return result
    }


    companion object {

        const val TABLE_ID = 15

        const val STATUS_ATTENDED = 1

        const val STATUS_ABSENT = 2

        const val STATUS_PARTIAL = 4
    }


}
