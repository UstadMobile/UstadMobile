package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*


@Entity
class ScheduledCheck() {

    @PrimaryKey(autoGenerate = true)
    var scheduledCheckUid: Long = 0

    var checkTime: Long = 0

    var checkType: Int = 0

    var checkUuid: String? = null

    var checkParameters: String? = null

    var scClazzLogUid: Long = 0

    @MasterChangeSeqNum
    var scheduledCheckMasterCsn: Long = 0

    @LocalChangeSeqNum
    var scheduledCheckLocalCsn: Long = 0

    @LastChangedBy
    var scheduledCheckLastChangedBy: Int = 0

    @LastChangedTime
    var scheduledCheckLct: Long = 0

    constructor(checkTime: Long, checkType: Int, checkParameters: String): this() {
        this.checkTime = checkTime
        this.checkType = checkType
        this.checkParameters = checkParameters
    }

    constructor(checkTime: Long, checkType: Int, clazzLogUid: Long): this() {
        this.checkTime = checkTime
        this.checkType = checkType
        this.scClazzLogUid = clazzLogUid
    }

    companion object {

        /**
         * Generate a FeedEntry for the teacher if attendance has not yet been recorded (generated at
         * the time class is due to start)
         */
        val TYPE_RECORD_ATTENDANCE_REMINDER = 1

        //eg: An alert if a teacher did not take attendance by the next day.
        val TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER = 2

        val TYPE_CHECK_ATTENDANCE_VARIATION_HIGH = 3
        val TYPE_CHECK_ATTENDANCE_VARIATION_MED = 6
        val TYPE_CHECK_ATTENDANCE_VARIATION_LOW = 7

        val TYPE_CHECK_PARTIAL_REPETITION_MED = 4
        val TYPE_CHECK_ABSENT_REPETITION_LOW = 5
        val TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER = 5
        //eg: An alert for a student that has been absent 2 or more days in a row
        val TYPE_CHECK_ABSENT_REPETITION_MED = 9

        //eg: An alert for classrooms with an average of 6% attendance or less.
        val TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH = 8

        //eg: An alert to show which student or teacher profiles were created or updated.
        val TYPE_CHECK_PERSON_PROFILE_UPDATED = 10

        //eg: An alert when a student has not attended in a single day in a month(drop-out)
        val TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH = 11

        val PARAM_CLAZZ_UID = "clazzuid"

        val PARAM_CLAZZ_LOG_UID = "clazzloguid"
    }
}
