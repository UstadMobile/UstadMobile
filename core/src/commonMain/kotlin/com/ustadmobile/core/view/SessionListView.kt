package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay


interface SessionListView: UstadListView<PersonWithSessionsDisplay, PersonWithSessionsDisplay> {

    var personWithContentTitle: String?

    companion object {
        const val VIEW_NAME = "PersonWithSessionListView"

        const val ARG_CONTEXT_REGISTRATION = "contextRegistration"
    }

}
