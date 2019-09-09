package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SelQuestion

interface SELQuestionSetDetailView : UstadView {
    fun finish()
    fun setListProvider(listProvider: DataSource.Factory<Int, SelQuestion>)
    fun updateToolbarTitle(title: String)

    companion object {
        val VIEW_NAME = "SELQuestionSetDetailView"
        val ARG_SEL_QUESTION_SET_UID = "SELQuestionSetUid"
        val ARG_SEL_QUESTION_SET_NAME = "SELQuestionSetName"
    }
}
