package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
open class ReportFilter {

    var reportFilterUid: Int = 0

    var reportFilterSeriesUid: Int = 0

    var reportFilterField: Int = FIELD_PERSON_GENDER

    var reportFilterCondition: Int = 0

    var reportFilterValue: String? = null

    var reportFilterDropDownValue: Int = 0

    var reportFilterValueBetweenX: String? = null

    var reportFilterValueBetweenY: String? = null



    companion object {

        const val FIELD_PERSON_GENDER = 100

        const val FIELD_PERSON_AGE = 101

        const val FIELD_CONTENT_COMPLETION = 102

        const val FIELD_CONTENT_ENTRY = 103

        const val FIELD_CONTENT_PROGRESS = 104

        const val FIELD_ATTENDANCE_PERCENTAGE = 105

        const val FIELD_CLAZZ_ENROLMENT_OUTCOME = 106

        const val FIELD_CLAZZ_ENROLMENT_LEAVING_REASON = 107

        const val CONDITION_IS = 200

        const val CONDITION_IS_NOT = 201

        const val CONDITION_GREATER_THAN = 202

        const val CONDITION_LESS_THAN = 203

        const val CONDITION_BETWEEN = 205

        const val CONDITION_IN_LIST = 206

        const val CONDITION_NOT_IN_LIST = 207

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ReportFilter

        if (reportFilterUid != other.reportFilterUid) return false
        if (reportFilterSeriesUid != other.reportFilterSeriesUid) return false
        if (reportFilterField != other.reportFilterField) return false
        if (reportFilterCondition != other.reportFilterCondition) return false
        if (reportFilterValue != other.reportFilterValue) return false
        if (reportFilterDropDownValue != other.reportFilterDropDownValue) return false
        if (reportFilterValueBetweenX != other.reportFilterValueBetweenX) return false
        if (reportFilterValueBetweenY != other.reportFilterValueBetweenY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reportFilterUid
        result = 31 * result + reportFilterSeriesUid
        result = 31 * result + reportFilterField
        result = 31 * result + reportFilterCondition
        result = 31 * result + (reportFilterValue?.hashCode() ?: 0)
        result = 31 * result + reportFilterDropDownValue
        result = 31 * result + (reportFilterValueBetweenX?.hashCode() ?: 0)
        result = 31 * result + (reportFilterValueBetweenY?.hashCode() ?: 0)
        return result
    }


}