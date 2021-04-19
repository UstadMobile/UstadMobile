package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity
class UstadCentralReportRow {

    var instanceId: Int = 0

    var indicatorId: Int = 0

    var disaggregationKey: Int = 0

    var disaggregationValue: Int = 0

    var value: Double = 0.0

    var valueStr: String? = null

    var timestamp: Long = 0L

    companion object {

        const val TOTAL_KEY = 1

        const val GENDER_KEY = 2

        const val COUNTRY_KEY = 3

        const val CONNECTIVITY_KEY = 4

        const val REGISTERED_USERS_INDICATOR = 100

        const val ACTIVE_USERS_INDICATOR = 101

        const val ACTIVE_USER_DURATION_INDICATOR = 102

        const val ACTIVE_USER_COMPLETED_ASSIGNMENT_INDICATOR = 103

    }
}