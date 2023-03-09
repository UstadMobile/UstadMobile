package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithSessionsDisplay {

    var startDate: Long = 0L

    var contextRegistration: String? = null

    var duration: Long = 0

    var resultSuccess: Byte = StatementEntity.RESULT_UNSET

    var resultComplete: Boolean = false

    var resultScoreScaled: Float = 0f

    var resultMax: Int = 0

    var resultScore: Int = 0

    companion object {

        const val RESULT_SUCCESS = 1

        const val RESULT_FAILURE = 2

        const val RESULT_UNSET = 3

        const val RESULT_INCOMPLETE = 5

    }
}