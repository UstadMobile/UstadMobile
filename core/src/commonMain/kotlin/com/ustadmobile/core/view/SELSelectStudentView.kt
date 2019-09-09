package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Person

/**
 * View responsible for selecting student that will do the SEL nominations.
 * SELSelectStudentView Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELSelectStudentView : UstadView {

    /**
     * Sets Current SEL answers by students list
     *
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param selStudentsProvider The provider data
     */
    fun setSELAnswerListProvider(selStudentsProvider: DataSource.Factory<Int, Person>)

    /**
     * Finish the view (closes it)
     */
    fun finish()

    /**
     * Set all question sets to the view to be selected.
     *
     * @param presets   The string array in order to be populated in the Question Set title
     * change drop down / spinner
     */
    fun setQuestionSetDropdownPresets(presets: Array<String>)

    companion object {

        //View name
        val VIEW_NAME = "SELSelectStudent"

        //Arguments
        val ARG_STUDENT_DONE = "studentDone"
        val ARG_DONE_CLAZZMEMBER_UIDS = "donePersonUids"
        val ARG_SELECTED_QUESTION_SET_UID = "questionSetUid"
    }


}
