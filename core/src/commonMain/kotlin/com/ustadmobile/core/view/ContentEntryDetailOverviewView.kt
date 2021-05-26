package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.*


interface ContentEntryDetailOverviewView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>?

    /**
     * Show the download dialog. The platform should request permission before if required.
     */
    fun showDownloadDialog(args: Map<String, String>)

    var downloadJobItem: DownloadJobItem?

    var scoreProgress: ContentEntryStatementScoreProgress?

    var locallyAvailable: Boolean

    var markCompleteVisible: Boolean

    companion object {

        const val VIEW_NAME = "ContentEntryDetailOverviewView"
    }

}