package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonGroup


interface PersonGroupEditView: UstadEditView<PersonGroup> {

    companion object {

        const val VIEW_NAME = "PersonGroupEditView"

    }

}