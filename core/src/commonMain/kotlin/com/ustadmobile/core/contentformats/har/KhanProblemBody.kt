package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
class KhanProblemBody {

    var operationName: String? = null

    var variables: Variable? = null

    @Serializable
    class Variable {

        var input: Input? = null

        @Serializable
        class Input {

            var problemNumber: Int? = 0

            var exerciseId: String? = null

            var timeTaken: Int = 0

            var countHints: Int = 0

            var completed: Boolean = false

            var itemId: String? = null

            var assessmentItemId: String? = null

            var quizProblemNumber: String? = null

            var topicId: String? = null

            var skipped: Boolean = false

        }

    }

}