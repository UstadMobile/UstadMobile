package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.@Entity@
import com.ustadmobile.lib.db.entities.@DisplayEntity@

interface @Entity@ListView: UstadListView<@Entity@, @DisplayEntity@> {

    companion object {
        const val VIEW_NAME = "@Entity@ListView"
    }

}