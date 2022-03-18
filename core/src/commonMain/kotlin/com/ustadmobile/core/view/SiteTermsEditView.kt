package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


interface SiteTermsEditView: UstadEditView<SiteTermsWithLanguage> {

    var languageError: String?

    companion object {

        const val VIEW_NAME = "SiteTermsEditView"

    }

}