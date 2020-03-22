package com.ustadmobile.lib.contentscrapers.voa

class VoaQuiz {

    var quizId: String? = null

    var questions: List<Questions>? = null

    class Questions {

        var questionText: String? = null

        var videoHref: String? = null

        var answerId: String? = null

        var answer: String? = null

        var choices: List<Choices>? = null

        class Choices {

            var id: String? = null

            var answerText: String? = null

        }
    }
}
