package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PersonWithEnrollment : Person() {

    var clazzUid: Long = 0

    var enrolled: Boolean? = null

    var attendancePercentage: Float = 0.toFloat()

    var clazzMemberRole: Int = 0

    var clazzName: String? = null

    var personPictureUid: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PersonWithEnrollment

        if (clazzUid != other.clazzUid) return false
        if (enrolled != other.enrolled) return false
        if (attendancePercentage != other.attendancePercentage) return false
        if (clazzMemberRole != other.clazzMemberRole) return false
        if (clazzName != other.clazzName) return false
        if (personPictureUid != other.personPictureUid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzUid.hashCode()
        result = 31 * result + (enrolled?.hashCode() ?: 0)
        result = 31 * result + attendancePercentage.hashCode()
        result = 31 * result + clazzMemberRole
        result = 31 * result + (clazzName?.hashCode() ?: 0)
        result = 31 * result + personPictureUid.hashCode()
        return result
    }
}
