package com.ustadmobile.staging.core.view

import com.ustadmobile.core.view.UstadView


/**
 * View represents editing SEL Questions that will be asked as per the SEL tasks.
 * SELQuestionEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELQuestionEditView : UstadView {

    /**
     * Closes the view.
     */
    fun finish()

    companion object {

        //View name
        val VIEW_NAME = "SELQuestionEdit"
    }


}
