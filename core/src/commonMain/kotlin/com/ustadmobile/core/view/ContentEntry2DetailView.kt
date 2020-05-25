package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer


interface ContentEntry2DetailView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>?)

    companion object {

        const val VIEW_NAME = "ContentEntryDetailView"
    }

}