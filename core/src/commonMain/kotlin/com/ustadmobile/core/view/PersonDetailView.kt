package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}