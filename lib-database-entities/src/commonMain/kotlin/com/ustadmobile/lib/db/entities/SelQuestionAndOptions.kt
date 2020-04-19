package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class SelQuestionAndOptions(
        @Embedded var selQuestion : SelQuestion,
        var options: List<SelQuestionOption>?)