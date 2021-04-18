package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary


interface ContentEntryDetailAttemptsListView: UstadListView<PersonWithAttemptsSummary, PersonWithAttemptsSummary> {

    companion object {
        const val VIEW_NAME = "PersonWithStatementDisplayListView"
    }

}