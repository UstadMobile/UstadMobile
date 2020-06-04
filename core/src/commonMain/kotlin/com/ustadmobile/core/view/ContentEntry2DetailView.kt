package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer


interface ContentEntry2DetailView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>?

    var downloadOptions: Map<String, String>?

    companion object {

        const val VIEW_NAME = "ContentEntryDetailView"
    }

}