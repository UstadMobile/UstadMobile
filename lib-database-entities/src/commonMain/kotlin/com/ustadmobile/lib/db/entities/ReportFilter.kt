package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
open class ReportFilter {

    var reportFilterUid: Long = 0

    var reportFilterSeriesUid: Long = 0

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

        const val FIELD_CLAZZ_ENROLMENT_STATUS = 106

        const val FIELD_CLAZZ_ENROLMENT_LEAVING_REASON = 107

        const val CONDITION_IS = 200

        const val CONDITION_IS_NOT = 201

        const val CONDITION_GREATER_THAN = 202

        const val CONDITION_LESS_THAN = 203

        const val CONDITION_BETWEEN = 205

        const val CONDITION_IN_LIST = 206

        const val CONDITION_NOT_IN_LIST = 207

    }


}