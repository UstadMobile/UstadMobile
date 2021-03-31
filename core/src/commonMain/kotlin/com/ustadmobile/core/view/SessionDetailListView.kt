package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithSessionDetailDisplay


interface SessionDetailListView: UstadListView<PersonWithSessionDetailDisplay, PersonWithSessionDetailDisplay> {

    var personWithContentTitle: String?

    companion object {

        const val VIEW_NAME = "SessionDetailListView"

    }

}
