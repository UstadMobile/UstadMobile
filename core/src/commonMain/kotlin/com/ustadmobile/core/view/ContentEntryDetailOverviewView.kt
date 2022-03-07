package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.ContentJobItemProgress


interface ContentEntryDetailOverviewView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    var availableTranslationsList: DoorDataSourceFactory<Int, ContentEntryRelatedEntryJoinWithLanguage>?

    /**
     * Show the download dialog. The platform should request permission before if required.
     */
    fun showDownloadDialog(args: Map<String, String>)

    var scoreProgress: ContentEntryStatementScoreProgress?

    var locallyAvailable: Boolean

    var markCompleteVisible: Boolean

    var showDownloadButton: Boolean

    var showUpdateButton: Boolean

    var showDeleteButton: Boolean

    var showManageDownloadButton: Boolean

    var showOpenButton: Boolean

    var contentJobItemStatus: Int

    var contentJobItemProgress: List<ContentJobItemProgress>?

    companion object {

        const val VIEW_NAME = "ContentEntryDetailOverviewView"
    }

}