package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.School.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class School() {

    @PrimaryKey(autoGenerate = true)
    var schoolUid: Long = 0

    var schoolName: String? = null

    var schoolDesc: String? = null

    var schoolAddress : String? = null

    //Active
    var schoolActive: Boolean = false

    // Features - bit mask
    var schoolFeatures: Long = 0

    //Location (precise) - longitude
    var schoolLocationLong : Double = 0.0

    //Location (precise) - latitude
    var schoolLocationLatt : Double = 0.0

    @MasterChangeSeqNum
    var schoolMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var schoolLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolLastChangedBy: Int = 0

    constructor(schoolName: String) : this() {
        this.schoolName = schoolName
        this.schoolActive = false
    }

    companion object {

        const val TABLE_ID = 164

        const val SCHOOL_FEATURE_ATTENDANCE: Long  = 1

        const val SCHOOL_GENDER_MALE : Int = 1
        const val SCHOOL_GENDER_FEMALE : Int = 2
        const val SCHOOL_GENDER_MIXED : Int = 3

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


}
