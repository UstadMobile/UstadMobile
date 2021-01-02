package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


interface SiteTermsEditView: UstadEditView<SiteTermsWithLanguage> {

    companion object {

        const val VIEW_NAME = "WorkspaceTermsEditEditView"

    }

}