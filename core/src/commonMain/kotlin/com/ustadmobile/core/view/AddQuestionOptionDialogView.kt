package com.ustadmobile.core.view

interface AddQuestionOptionDialogView : UstadView {
    /**
     * If you want to call view's direct finish method. (Usually used to well, finish the screen/view)
     */
    fun finish()


    fun setOptionText(text: String)

    companion object {
        val VIEW_NAME = "AddQuestionOptionDialogView"
        val ARG_QUESTION_OPTION_UID = "ArgQuestionOptionUid"
    }
}
