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

    var attendanceAverage: Float = -1f

    //Gives the DateRange calendar Uid
    var clazzHolidayUMCalendarUid: Long = 0

    //Gives the schedule calendar uid
    var clazzScheuleUMCalendarUid: Long = 0

    //Active
    var isClazzActive: Boolean = false

    //Location
    var clazzLocationUid: Long = 0

    var clazzStartTime: Long = 0

    var clazzEndTime: Long = 0

    //Clazz features
    var clazzFeatures: Long = (CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ASSIGNMENT)

    var clazzSchoolUid : Long = 0L

    @MasterChangeSeqNum
    var clazzMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLastChangedBy: Int = 0

    /**
     * The timezone ID as per https://www.iana.org/time-zones. If null, this means use the School
     * timezone
     */
    var clazzTimeZone: String? = null

    constructor(clazzName: String) : this() {
        this.clazzName = clazzName
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY  or CLAZZ_FEATURE_ASSIGNMENT
        this.isClazzActive = true
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY or CLAZZ_FEATURE_ASSIGNMENT
        this.isClazzActive = true
    }

    companion object {

        const val TABLE_ID = 6
        const val CLAZZ_FEATURE_ATTENDANCE = 1L
        const val CLAZZ_FEATURE_ACTIVITY = 4L
        const val CLAZZ_FEATURE_ASSIGNMENT = 8L
    }
}
