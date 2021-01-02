package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTerms


interface SiteDetailView: UstadDetailView<Site> {

    var siteTermsList: DataSource.Factory<Int, SiteTerms>?

    companion object {

        const val VIEW_NAME = "WorkspaceDetailView"

        /**
         *
         */
        const val ARG_SHOW_BY_DISPLAYED_LOCALE  = "showByDisplayedLocale"

    }

}