package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@

interface @Entity@DetailView: UstadDetailView<@DisplayEntity@> {

    companion object {

        const val VIEW_NAME = "@Entity@DetailView"

    }

}