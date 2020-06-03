package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class ClazzWorkQuestionAndOptionWithResponse(
    var clazzWork: ClazzWorkWithSubmission,
    var clazzWorkQuestion: ClazzWorkQuestion,
    var options: List<ClazzWorkQuestionOption>,
    var clazzWorkQuestionResponse: ClazzWorkQuestionResponse)