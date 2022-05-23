package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@

interface @BaseFileName@View: UstadListView<@Entity@, @DisplayEntity@> {

    companion object {
        const val VIEW_NAME = "@Entity@List"
    }

}