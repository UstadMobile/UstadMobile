package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language
import kotlin.js.JsName

interface ContentEntryListFragmentView : UstadView {

    @JsName("setContentEntryProvider")
    fun setContentEntryProvider(entryProvider: DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>)

    @JsName("setToolbarTitle")
    fun setToolbarTitle(title: String)

    @JsName("showError")
    fun showError()

    @JsName("setCategorySchemaSpinner")
    fun setCategorySchemaSpinner(spinnerData: Map<Long, List<DistinctCategorySchema>>)

    @JsName("setLanguageOptions")
    fun setLanguageOptions(result: List<Language>)

    companion object {

        const val VIEW_NAME = "ContentEntryList"
    }
}
