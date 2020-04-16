package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SELQuestionSetWithNumQuestions : SelQuestionSet() {

    var numQuestions: Int = 0
}
