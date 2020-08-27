package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzWorkQuestionAndOptionRow{
        @Embedded
        var clazzWorkQuestion: ClazzWorkQuestion?

        @Embedded
        var clazzWorkQuestionOption: ClazzWorkQuestionOption?

        constructor(q: ClazzWorkQuestion, o : ClazzWorkQuestionOption){
                clazzWorkQuestion = q
                clazzWorkQuestionOption = o
        }
        constructor(){
                clazzWorkQuestion = null
                clazzWorkQuestionOption = null
        }
}