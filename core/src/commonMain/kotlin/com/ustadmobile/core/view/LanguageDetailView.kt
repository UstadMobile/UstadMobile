package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Site

interface LanguageDetailView: UstadEditView<Site> {

    var langNameError: String?

    companion object {

        const val VIEW_NAME = "LanguageDetailView"

    }

}