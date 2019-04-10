package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language

interface ContentEntryListView : UstadView {

    fun setContentEntryProvider(entryProvider: UmProvider<ContentEntryWithStatusAndMostRecentContainerUid>)

    fun setToolbarTitle(title: String)

    fun showError()

    fun setCategorySchemaSpinner(spinnerData: Map<Long, List<DistinctCategorySchema>>)

    fun setLanguageOptions(result: List<Language>)

    companion object {

        const val VIEW_NAME = "ContentEntryList"
    }
}
