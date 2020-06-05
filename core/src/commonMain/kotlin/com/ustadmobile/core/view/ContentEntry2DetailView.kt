package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus


interface ContentEntry2DetailView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>?

    var downloadOptions: Map<String, String>?

    var downloadJobItem: DownloadJobItem?

    companion object {

        const val VIEW_NAME = "ContentEntryDetailView"
    }

}