package com.ustadmobile.lib.contentscrapers.ck12.practice

import com.google.gson.annotations.SerializedName

class QuestionResponse {

    var response: Response? = null

    inner class Response {

        @SerializedName("_id")
        var id: String? = null

        @SerializedName("goal")
        var goal: Int = 0

        @SerializedName("nextPracticeUrl")
        var nextPracticeUrl: String? = null

        @SerializedName("nextPracticeName")
        var nextPracticeName: String? = null

        @SerializedName("practiceName")
        var practiceName: String? = null

        @SerializedName("evalData")
        var data: String? = null

        @SerializedName("questionID")
        var questionID: String? = null

        @SerializedName("questionTypeName")
        var questionType: String? = null

        var stem: Question? = null

        internal var multiAnswers: Boolean = false

        var responseObjects: List<QuestionObjects>? = null

        var hints: MutableList<String>? = null

        var answer: AnswerResponse? = null

        inner class Question {

            var displayText: String? = null

        }

        inner class QuestionObjects {

            var displayOrder: Int = 0

            var displayText: String? = null

            var orderText: String? = null

            var optionKey: String? = null

        }

    }

}
