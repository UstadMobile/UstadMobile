package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.*

data class ContentEntryDetailOverviewUiState(

    val contentEntryButtons: ContentEntryButtonModel? = null,

    val availableTranslationsMap: List<ContentEntryRelatedEntryJoinWithLanguage> = emptyList(),

    val scoreProgress: ContentEntryStatementScoreProgress? = null,

    val contentEntry: ContentEntryWithMostRecentContainer? = null,

    val locallyAvailable: Boolean = false,

    val markCompleteVisible: Boolean = false,

    val translationVisibile: Boolean = false,

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
}