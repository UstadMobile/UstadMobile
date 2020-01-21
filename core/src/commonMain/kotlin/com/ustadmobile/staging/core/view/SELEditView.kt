package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture

/**
 * View for editing an SEL nomination. SELEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELEditView : UstadView {

    /**
     * Sets Current provider This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, PersonWithPersonPicture>)

    /**
     * Updates the heading on the SEL Edit page. Usually the question text.
     *
     * @param questionText      The question text.
     */
    fun updateHeading(questionText: String)

    /**
     * Updates the question count on the SEL Edit page. Usually the question counter/total
     *
     * @param iNum      The current question number
     * @param tNum      The total questions in the question set
     */
    fun updateHeading(iNum: String, tNum: String)

    /**
     * Closes the view.
     */
    fun finish()

    companion object {

        //View name
        val VIEW_NAME = "SELEdit"

        //Arguments
        val ARG_QUESTION_SET_UID = "questionSetUid"
        val ARG_QUESTION_UID = "questionUid"
        val ARG_CLAZZMEMBER_UID = "clazzMemberUid"
        val ARG_QUESTION_INDEX_ID = "questionIndexId"
        val ARG_QUESTION_SET_RESPONSE_UID = "questionSetResponseUid"
        val ARG_QUESTION_RESPONSE_UID = "questionResponseUid"
    }

}
