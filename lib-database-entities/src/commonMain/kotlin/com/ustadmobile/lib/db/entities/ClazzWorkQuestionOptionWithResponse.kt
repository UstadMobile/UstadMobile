package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class ClazzWorkQuestionOptionWithResponse(
        @Embedded var clazzWorkQuestionOption: ClazzWorkQuestionOption,
        var clazzWorkQuestionResponse: ClazzWorkQuestionResponse)