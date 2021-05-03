package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzAssignmentWithMetrics : ClazzAssignment() {

    @Embedded
    var studentProgress: StudentAssignmentProgress? = null

    var resultScoreScaled: Float = 0f

    var resultMax: Int = 0

    var resultScore: Int = 0

    var completedContent: Boolean = false

}