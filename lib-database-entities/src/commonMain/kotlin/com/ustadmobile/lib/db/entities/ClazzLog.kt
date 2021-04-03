package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * Represents one session where a class is expected to happen (e.g. an occurence of the class). It
 * is created either by ClazzLogCreator according to a preset schedule for the class, or by the user
 * manually. This is linked to attendance records (ClazzLogAttendanceRecord). It could also be related
 * to any other item that is related to an occurence of the class (e.g. behavior logs).
 */
@SyncableEntity(tableId = ClazzLog.TABLE_ID,
    notifyOnUpdate = [
        """
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${ClazzLog.TABLE_ID} AS tableId FROM 
        ChangeLog
        JOIN ClazzLog ON ChangeLog.chTableId = ${ClazzLog.TABLE_ID} AND ClazzLog.clazzLogUid = ChangeLog.chEntityPk
        JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid 
        JOIN Person ON Person.personUid IN (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1}  ${Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        """
    ],
    syncFindAllQuery =
        """
        SELECT ClazzLog.* FROM
        ClazzLog
        JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid
        JOIN Person ON Person.personUid IN  (${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT1} ${Role.PERMISSION_CLAZZ_SELECT } ${Clazz.ENTITY_PERSONS_WITH_PERMISSION_PT2})
        JOIN DeviceSession ON DeviceSession.dsPersonUid = Person.personUid
        WHERE DeviceSession.dsDeviceId = :clientId
        """
)
@Entity
@Serializable
open class ClazzLog()  {

    @PrimaryKey(autoGenerate = true)
    var clazzLogUid: Long = 0

    var clazzLogClazzUid: Long = 0

    var logDate: Long = 0

    var timeRecorded: Long = 0

    var clazzLogDone: Boolean = false

    var cancellationNote: String? = null

    var clazzLogCancelled: Boolean = false

    var clazzLogNumPresent: Int = 0

    var clazzLogNumAbsent: Int = 0

    var clazzLogNumPartial: Int = 0

    var clazzLogScheduleUid: Long = 0

    var clazzLogStatusFlag: Int = 0

    @MasterChangeSeqNum
    var clazzLogMSQN: Long = 0

    @LocalChangeSeqNum
    var clazzLogLCSN: Long = 0

    @LastChangedBy
    var clazzLogLCB: Int = 0

    constructor(clazzLogUid: Long, clazzUid: Long, logDate: Long, scheduleUid: Long): this() {
        this.clazzLogUid = clazzLogUid
        this.clazzLogClazzUid = clazzUid
        this.logDate = logDate
        this.clazzLogScheduleUid = scheduleUid
    }

    companion object {

        const val TABLE_ID = 14

        const val STATUS_CREATED = 0

        const val STATUS_HOLIDAY = 1

        const val STATUS_MANUALLYCANCELED = 2

        const val STATUS_RECORDED = 4

        const val STATUS_RESCHEDULED = 8

        const val STATUS_INACTIVE = 16
    }


}
