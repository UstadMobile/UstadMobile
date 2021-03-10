package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.SiteTerms


interface SiteTermsDetailView: UstadDetailView<SiteTerms> {

    var acceptButtonVisible: Boolean

    companion object {

        const val VIEW_NAME = "SiteTermsDetailView"

        //Additional alias view name - set to hide bottom navigation
        const val VIEW_NAME_ACCEPT_TERMS = "SiteTermsDetailAcceptTerms"

        /**
         * If this argument is provided and true, then terms will be looked up according to the
         * user's locale. No entity uid is required for this.
         */
        const val ARG_USE_DISPLAY_LOCALE = "useDisplayLocale"

        /**
         * This argument will trigger the display of an accept button. When the user clicks the
         * button, the user will be taken to PersonEditView
         */
        const val ARG_SHOW_ACCEPT_BUTTON = "showAccept"


    }

}