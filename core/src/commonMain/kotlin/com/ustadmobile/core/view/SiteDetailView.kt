package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


interface SiteDetailView: UstadDetailView<Site> {

    var siteTermsList: DataSourceFactory<Int, SiteTermsWithLanguage>?

    companion object {

        const val VIEW_NAME = "SiteDetailView"

        /**
         *
         */
        const val ARG_SHOW_BY_DISPLAYED_LOCALE  = "showByDisplayedLocale"

    }

}