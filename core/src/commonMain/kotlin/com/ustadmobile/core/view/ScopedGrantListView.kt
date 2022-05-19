package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantWithName

interface ScopedGrantListView: UstadListView<ScopedGrant, ScopedGrantWithName> {

    companion object {

        const val VIEW_NAME = "ScopedGrantList"

        const val ARG_FILTER_TABLE_ID = "filterTable"

        const val ARG_FILTER_ENTITY_UID = "filterEntity"

    }

}