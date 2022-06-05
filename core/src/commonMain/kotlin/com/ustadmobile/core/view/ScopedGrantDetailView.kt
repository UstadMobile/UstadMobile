package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ScopedGrantWithName


interface ScopedGrantDetailView: UstadDetailView<ScopedGrantWithName> {

    companion object {

        const val VIEW_NAME = "ScopedGrantDetail"

    }

}