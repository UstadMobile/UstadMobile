package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable


@Entity
@Serializable
@ReplicateEntity(
    tableId = School.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "school_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT],
 )
))
open class School() {

    @PrimaryKey(autoGenerate = true)
    var schoolUid: Long = 0

    var schoolName: String? = null

    var schoolDesc: String? = null

    var schoolAddress : String? = null

    //Active
    var schoolActive: Boolean = false

    var schoolPhoneNumber : String? = null

    var schoolGender : Int = 0

    var schoolHolidayCalendarUid: Long = 0L

    // Features - bit mask
    var schoolFeatures: Long = 0

    //Location (precise) - longitude
    var schoolLocationLong : Double = 0.0

    //Location (precise) - latitude
    var schoolLocationLatt : Double = 0.0

    var schoolEmailAddress : String?= null

    var schoolTeachersPersonGroupUid: Long = 0

    var schoolStudentsPersonGroupUid: Long = 0

    var schoolPendingStudentsPersonGroupUid: Long = 0

    var schoolCode: String? = null

    @MasterChangeSeqNum
    var schoolMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var schoolLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var schoolLct: Long = 0


    /**
     * The timezone ID as per https://www.iana.org/time-zones. If null, this means use the School
     * timezone
     */
    var schoolTimeZone: String? = null

    constructor(schoolName: String) : this() {
        this.schoolName = schoolName
        this.schoolActive = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as School

        if (schoolUid != other.schoolUid) return false
        if (schoolName != other.schoolName) return false
        if (schoolDesc != other.schoolDesc) return false
        if (schoolActive != other.schoolActive) return false
        if (schoolFeatures != other.schoolFeatures) return false
        if (schoolLocationLong != other.schoolLocationLong) return false
        if (schoolLocationLatt != other.schoolLocationLatt) return false
        if( schoolAddress != other.schoolAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = schoolUid.hashCode()
        result = 31 * result + (schoolName?.hashCode() ?: 0)
        result = 31 * result + (schoolDesc?.hashCode() ?: 0)
        result = 31 * result + schoolActive.hashCode()
        result = 31 * result + schoolFeatures.hashCode()
        result = 31 * result + schoolLocationLong.hashCode()
        result = 31 * result + schoolLocationLatt.hashCode()
        result = 31 * result + schoolAddress.hashCode()
        return result
    }

    companion object {
        const val TABLE_ID = 164

    }

}
