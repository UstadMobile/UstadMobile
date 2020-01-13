package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SELNominationItem() {
    var clazzName: String? = null
    var questionSetTitle: String? = null
    var nominatorName: String? = null
    var nomineeName: String? = null
    var questionText: String? = null
    var nomineeUid: Long = 0
    var nominatorUid: Long = 0
    var clazzUid: Long = 0
    var selQuestionUid: Long = 0
}
