package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithSessionsDisplay {

    var startDate: Long = 0L

    var contextRegistration: String? = null

    var duration: Long = 0

    var resultSuccess: Int = 0

    var resultComplete: Boolean = false

    var resultScoreScaled: Float = 0f

    var resultMax: Int = 0

    var resultScore: Int = 0

}