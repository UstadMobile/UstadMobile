package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionOption

interface SELQuestionDetail2View : UstadView {
    fun setQuestionOptionsProvider(listProvider: DataSource.Factory<Int, SelQuestionOption>)
    fun setQuestionTypePresets(presets: Array<String>)
    fun finish()
    fun setQuestionText(questionText: String)
    fun setQuestionType(type: Int)
    fun handleClickDone()
    fun showQuestionOptions(show: Boolean)
    fun handleQuestionTypeChange(type: Int)
    fun handleClickAddOption()
    fun setQuestionTypeListener()
    fun setQuestionOnView(selQuestion: SelQuestion)

    companion object {
        val VIEW_NAME = "SELQuestionDetail2"
        val ARG_QUESTION_UID_QUESTION_DETAIL = "ARGQuestionUidForQuestionDetail"
        val ARG_QUESTION_OPTION_UID = "ArgQuestionOptionUid"
    }
}
