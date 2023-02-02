package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay
import com.ustadmobile.lib.db.entities.VerbEntity.Companion.VERB_ANSWERED_UID
import kotlin.jvm.JvmInline

data class StatementListUiState(

    val statementList: List<StatementWithSessionDetailDisplay> = emptyList(),

)

val StatementWithSessionDetailDisplay.listItemUiState
    get() = StatementWithSessionDetailDisplayUiState(this)

@JvmInline
value class StatementWithSessionDetailDisplayUiState(
    val statement: StatementWithSessionDetailDisplay,
) {

    val personVerbTitleText: String?
        get() = if (statement.verbDisplay != null)
            statement.verbDisplay?.replaceFirstChar { it.titlecase() }
        else
            statement.verbDisplay

    val descriptionVisible: Boolean
        get() = !statement.objectDisplay.isNullOrBlank()

    val questionAnswerVisible: Boolean
        get() = statement.statementVerbUid == VERB_ANSWERED_UID

    val resultScoreMaxVisible: Boolean
        get() = statement.resultScoreMax > 0

    val scoreResultsText: String
        get() = "(${statement.resultScoreRaw}/${statement.resultScoreMax})"

}
