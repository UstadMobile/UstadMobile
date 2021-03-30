package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay


interface SessionsListView: UstadListView<PersonWithSessionsDisplay, PersonWithSessionsDisplay> {

    var personWithContentTitle: String?

    companion object {
        const val VIEW_NAME = "PersonWithSessionListView"
    }

}
