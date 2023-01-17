package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import kotlin.jvm.JvmInline

data class SessionListUiState (

    val personWithContentTitle: String = "",

    val sessionsList: List<PersonWithSessionsDisplay> = emptyList()

)

val PersonWithSessionsDisplay.listItemUiState
    get() = SessionListItemUiState(this)

@JvmInline
value class SessionListItemUiState(
    val person: PersonWithSessionsDisplay,
) {

    val scoreResultVisible: Boolean
        get() = person.resultScoreScaled != 0F

    val scoreResultText: String
        get() = "(" + person.resultScore + "/" + person.resultMax + ")"

}