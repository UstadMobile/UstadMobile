package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzAssignmentWithMetrics : ClazzAssignment() {

    var notSubmittedStudents : Int = 0

    var submittedStudents : Int = 0

    var completedStudents : Int = 0

    var resultScoreScaled: Float = 0f

    var resultMax: Int = 0

    var resultScore: Int = 0

    var completedContent: Boolean = false

}