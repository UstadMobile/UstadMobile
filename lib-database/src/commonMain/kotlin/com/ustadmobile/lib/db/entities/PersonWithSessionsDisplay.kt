package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.db.entities.xapi.StatementEntity
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

}