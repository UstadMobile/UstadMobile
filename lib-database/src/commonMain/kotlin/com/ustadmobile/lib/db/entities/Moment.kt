package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class Moment {

    var typeFlag: Int = TYPE_FLAG_FIXED

    var fixedTime: Long = 0

    var relTo: Int = TODAY_REL_TO

    var relOffSet: Int = -0

    var relUnit: Int = DAYS_REL_UNIT



    companion object {

        const val TYPE_FLAG_FIXED = 0

        const val TYPE_FLAG_RELATIVE = 1

        const val DAYS_REL_UNIT = 1

        const val WEEKS_REL_UNIT = 2

        const val MONTHS_REL_UNIT = 3

        const val YEARS_REL_UNIT = 4

        const val TODAY_REL_TO = 0

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Moment

        if (typeFlag != other.typeFlag) return false
        if (fixedTime != other.fixedTime) return false
        if (relTo != other.relTo) return false
        if (relOffSet != other.relOffSet) return false
        if (relUnit != other.relUnit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = typeFlag
        result = 31 * result + fixedTime.hashCode()
        result = 31 * result + relTo
        result = 31 * result + relOffSet
        result = 31 * result + relUnit
        return result
    }

}

@Serializable
data class DateRangeMoment(val fromMoment: Moment, val toMoment: Moment)

