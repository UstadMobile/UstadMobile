package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class ClazzWorkQuestionAndOptions(
        @Embedded var clazzWorkQuestion: ClazzWorkQuestion,
        var options: List<ClazzWorkQuestionOption>,
        var optionsToDeactivate: List<Long>)