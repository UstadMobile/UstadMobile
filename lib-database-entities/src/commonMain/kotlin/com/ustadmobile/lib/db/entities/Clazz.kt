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

    var clazzStartTime: Long = 0

    var clazzEndTime: Long = 0

    //Clazz features
    var clazzFeatures: Long = (CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ASSIGNMENT)

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

    fun isAttendanceFeature() : Boolean{
        return clazzFeatures and CLAZZ_FEATURE_ATTENDANCE > 0
    }
    fun isSelFeature() : Boolean{
        return clazzFeatures and CLAZZ_FEATURE_SEL > 0
    }
    fun isActivityFeature() : Boolean{
        return clazzFeatures and CLAZZ_FEATURE_ACTIVITY > 0
    }
    fun isAssignmentFeature() : Boolean{
        return clazzFeatures and CLAZZ_FEATURE_ASSIGNMENT > 0
    }

    fun updateAttendanceFeature(enabled: Boolean){
        if(!isAttendanceFeature()){
            if(enabled){
                clazzFeatures = clazzFeatures or CLAZZ_FEATURE_ATTENDANCE
            }
        }else{
            if(!enabled){
                clazzFeatures = clazzFeatures xor CLAZZ_FEATURE_ATTENDANCE
            }
        }
    }

    fun updateSelFeature(enabled: Boolean){
        if(!isSelFeature()){
            if(enabled){
                clazzFeatures = clazzFeatures or CLAZZ_FEATURE_SEL
            }
        }else{
            if(!enabled){
                clazzFeatures = clazzFeatures xor CLAZZ_FEATURE_SEL
            }
        }
    }

    fun updateActivityFeature(enabled: Boolean){
        if(!isActivityFeature()){
            if(enabled){
                clazzFeatures = clazzFeatures or CLAZZ_FEATURE_ACTIVITY
            }
        }else{
            if(!enabled){
                clazzFeatures = clazzFeatures xor CLAZZ_FEATURE_ACTIVITY
            }
        }
    }

    fun updateAssignmentFeature(enabled: Boolean){
        if(!isAssignmentFeature()){
            if(enabled){
                clazzFeatures = clazzFeatures or CLAZZ_FEATURE_ASSIGNMENT
            }
        }else{
            if(!enabled){
                clazzFeatures = clazzFeatures xor CLAZZ_FEATURE_ASSIGNMENT
            }
        }
    }

    constructor(clazzName: String) : this() {
        this.clazzName = clazzName
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY or
                CLAZZ_FEATURE_SEL or CLAZZ_FEATURE_ASSIGNMENT
        this.isClazzActive = true
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
        this.clazzFeatures = CLAZZ_FEATURE_ATTENDANCE or CLAZZ_FEATURE_ACTIVITY or
                CLAZZ_FEATURE_SEL or CLAZZ_FEATURE_ASSIGNMENT
        this.isClazzActive = true
    }

    companion object {

        const val TABLE_ID = 6
        const val CLAZZ_FEATURE_ATTENDANCE = 1L
        const val CLAZZ_FEATURE_SEL = 2L
        const val CLAZZ_FEATURE_ACTIVITY = 4L
        const val CLAZZ_FEATURE_ASSIGNMENT = 8L
    }
}
