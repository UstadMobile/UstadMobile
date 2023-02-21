package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ContentEntryDetailOverviewView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    var availableTranslationsList: DataSourceFactory<Int, ContentEntryRelatedEntryJoinWithLanguage>?

    /**
     * Show the download dialog. The platform should request permission before if required.
     */
    fun showDownloadDialog(args: Map<String, String>)

    var scoreProgress: ContentEntryStatementScoreProgress?

    var locallyAvailable: Boolean

    var markCompleteVisible: Boolean

    var contentEntryButtons: ContentEntryButtonModel?

    var activeContentJobItems: List<ContentJobItemProgress>?

    companion object {

        const val VIEW_NAME = "ContentEntryDetailOverviewView"
    }

}