package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWorkQuestionAndOptionWithResponseRow{
        @Embedded
        var clazzWorkQuestion: ClazzWorkQuestion?

        @Embedded
        var clazzWorkQuestionOption: ClazzWorkQuestionOption?

        @Embedded
        var clazzWorkQuestionOptionResponse: ClazzWorkQuestionResponse?

        constructor(q: ClazzWorkQuestion, o : ClazzWorkQuestionOption,
                    r: ClazzWorkQuestionResponse){
                clazzWorkQuestion = q
                clazzWorkQuestionOption = o
                clazzWorkQuestionOptionResponse = r
        }

        constructor(){
                clazzWorkQuestion = null
                clazzWorkQuestionOption = null
                clazzWorkQuestionOptionResponse = null
        }
}