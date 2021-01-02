package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.db.entities.SiteTerms


interface WorkspaceDetailView: UstadDetailView<WorkSpace> {

    var workspaceTermsList: DataSource.Factory<Int, SiteTerms>?

    companion object {

        const val VIEW_NAME = "WorkspaceDetailView"

        /**
         *
         */
        const val ARG_SHOW_BY_DISPLAYED_LOCALE  = "showByDisplayedLocale"

    }

}