package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Language


interface LanguageListView: UstadListView<Language, Language> {

    companion object {
        const val VIEW_NAME = "LanguageListView"
    }

}