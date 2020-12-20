package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
open class ReportFilter {

    var reportFilterUid: Long = 0

    var reportFilterSeriesUid: Long = 0

    var reportFilterField: Int = 0

    var reportFilterCondition: Int = 0

    var reportFilterValue: String? = null

    var reportFilterDropDownValue: Int = 0

    companion object {

        const val FIELD_PERSON_GENDER = 100

        const val FIELD_PERSON_AGE = 101


        const val CONDITION_IS = 200

        const val CONDITION_IS_NOT = 201

        const val CONDITION_GREATER_THAN = 202

        const val CONDITION_LESS_THAN = 203

        const val CONDITION_WITHIN_RANGE = 204


        const val VALUE_FLAG_INTEGER = 1

        const val VALUE_FLAG_DROPDOWN = 2

    }


}