package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Entity
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

interface EntityDetailView: UstadDetailView<ClazzWorkWithSubmission> {

    companion object {

        const val VIEW_NAME = "EntityDetailView"

    }

}