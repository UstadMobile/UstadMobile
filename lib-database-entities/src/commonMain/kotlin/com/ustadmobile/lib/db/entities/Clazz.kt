package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum
import com.ustadmobile.lib.db.entities.Clazz.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
open class Clazz() {

    @PrimaryKey(autoGenerate = true)
    var clazzUid: Long = 0

    var clazzName: String? = null

    var clazzDesc: String? = null

    var attendanceAverage: Float = 0.toFloat()

    //Gives the DateRange calendar Uid
    var clazzHolidayUMCalendarUid: Long = 0

    //Gives the schedule calendar uid
    var clazzScheuleUMCalendarUid: Long = 0

    //Active
    var isClazzActive: Boolean = false

    //Location
    var clazzLocationUid: Long = 0

    //Attendance
    var isAttendanceFeature: Boolean = true

    //Activity
    var isActivityFeature: Boolean = true

    //SEL
    var isSelFeature: Boolean = true

    var clazzStartTime: Long = 0

    var clazzEndTime: Long = 0

    @UmSyncMasterChangeSeqNum
    var clazzMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var clazzLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var clazzLastChangedBy: Int = 0


    constructor(clazzName: String) : this() {
        this.clazzName = clazzName
        this.isAttendanceFeature = true
        this.isActivityFeature = true
        this.isSelFeature = true
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
        this.isAttendanceFeature = true
        this.isActivityFeature = true
        this.isSelFeature = true
    }

    companion object {

        const val TABLE_ID = 6
    }
}
