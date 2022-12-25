package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.*

data class ContentEntryDetailOverviewUiState(

    val scoreProgress: ContentEntryStatementScoreProgress? = null,

    val contentEntry: ContentEntryWithMostRecentContainer? = null,

    val contentEntryButtons: ContentEntryButtonModel? = null,

    val locallyAvailable: Boolean = false,

    val markCompleteVisible: Boolean = false,

    val translationVisibile: Boolean = false,

    val availableTranslations: List<ContentEntryRelatedEntryJoinWithLanguage> = emptyList(),

    val activeContentJobItems: List<ContentJobItemProgress> = emptyList()

) {
    val scoreProgressVisible: Boolean
        get() = scoreProgress?.progress != null
                && scoreProgress.progress > 0

    val authorVisible: Boolean
        get() = !contentEntry?.author.isNullOrBlank()

    val publisherVisible: Boolean
        get() = !contentEntry?.publisher.isNullOrBlank()

    val licenseNameVisible: Boolean
        get() = !contentEntry?.licenseName.isNullOrBlank()

    val fileSizeVisible: Boolean
        get() = contentEntry?.container?.fileSize != null
                && contentEntry.container?.fileSize != 0L

    val scoreResultVisible: Boolean
        get() = scoreProgress != null

}