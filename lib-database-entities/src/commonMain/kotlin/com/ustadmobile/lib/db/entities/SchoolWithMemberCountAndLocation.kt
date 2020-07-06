package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SchoolWithMemberCountAndLocation() : School() {

    var numStudents: Int = 0

    var numTeachers: Int = 0

    var locationName: String? = null

    var clazzCount: Int = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as SchoolWithMemberCountAndLocation

        if (numStudents != other.numStudents) return false
        if (numTeachers != other.numTeachers) return false
        if (locationName != other.locationName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + numStudents
        result = 31 * result + numTeachers
        result = 31 * result + (locationName?.hashCode() ?: 0)
        return result
    }


}
