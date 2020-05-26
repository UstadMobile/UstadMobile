package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer


interface ContentEntry2DetailView: UstadDetailView<ContentEntryWithMostRecentContainer>, UstadBaseFeedbackMessageView {

    fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>?)

    fun navigateToTranslation(entryUid: Long)

    fun showDownloadOptionsDialog(map: Map<String, String>)

    companion object {

        const val VIEW_NAME = "ContentEntryDetailView"
    }

}