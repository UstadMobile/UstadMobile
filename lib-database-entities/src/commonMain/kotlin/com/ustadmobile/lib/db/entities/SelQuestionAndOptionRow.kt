package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
//
//@Serializable
//data class SelQuestionAndOptionRow(
//    @Embedded
//    var selQuestion: SelQuestion,
//
//    @Embedded
//    var selQuestionOption: SelQuestionOption
//)

@Serializable
class SelQuestionAndOptionRow{
        @Embedded
        var selQuestion: SelQuestion?

        @Embedded
        var selQuestionOption: SelQuestionOption?

        constructor(q: SelQuestion, o : SelQuestionOption){
                selQuestion = q
                selQuestionOption = o
        }
        constructor(){
                selQuestion = null
                selQuestionOption = null
        }
}