package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Clazz.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
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


    @MasterChangeSeqNum
    var clazzMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLastChangedBy: Int = 0


    constructor(clazzName: String) : this() {
        this.clazzName = clazzName
        this.isAttendanceFeature = true
        this.isActivityFeature = true
        this.isSelFeature = true
        this.isClazzActive = false
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
        this.isAttendanceFeature = true
        this.isActivityFeature = true
        this.isSelFeature = true
        this.isClazzActive = false
    }

    companion object {

        const val TABLE_ID = 6
    }
}
