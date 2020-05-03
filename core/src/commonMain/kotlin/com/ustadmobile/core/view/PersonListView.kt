package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonListView: UstadListView<Person, PersonWithDisplayDetails> {

    companion object {
        const val VIEW_NAME = "PersonListView"
    }

}