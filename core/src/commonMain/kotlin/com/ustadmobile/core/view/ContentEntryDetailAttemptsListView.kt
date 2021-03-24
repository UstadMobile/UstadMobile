package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithStatementDisplay


interface ContentEntryDetailAttemptsListView: UstadListView<PersonWithStatementDisplay, PersonWithStatementDisplay> {

    companion object {
        const val VIEW_NAME = "PersonWithStatementDisplayListView"
    }

}