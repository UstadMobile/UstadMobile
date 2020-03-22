package com.ustadmobile.lib.contentscrapers.ck12.practice

import com.google.gson.annotations.SerializedName

class AnswerResponse {

    @SerializedName("instance")
    var instance: Instance? = null

    inner class Instance {

        var solution: String? = null

        internal var multiAnswers: Boolean = false

        internal var questionTypeName: String? = null

        var responseObjects: List<AnswerObjects>? = null

        var answer: MutableList<Any>? = null

        inner class AnswerObjects {

            internal var isCorrect: String? = null

            var displayOrder: Int = 0

            var displayText: String? = null

            var orderText: String? = null

            var optionKey: String? = null

            var ansSeq: Int = 0

        }

    }

}
