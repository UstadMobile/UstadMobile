package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions

/**
 * Core View. Screen is for SEL Question list.
 */
interface SELQuestionSetsView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Sets the given provider to the view's provider adapter.
     * @param listProvider  The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, SELQuestionSetWithNumQuestions>)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "SELQuestionSets"

        //Any argument keys:
        val ARG_SEL_QUESTION_SET_UID = "ArgSELQuestionSetUid"
    }
}
