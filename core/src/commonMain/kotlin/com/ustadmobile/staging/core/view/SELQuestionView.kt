package com.ustadmobile.core.view


/**
 * View represents a Question view between SEL nominations.
 * SELQuestion Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SELQuestionView : UstadView {

    /**
     * Closes the view.
     */
    fun finish()

    /**
     * Updates the question on the view's toolbar
     *
     * @param questionText  The question text
     */
    fun updateQuestion(questionText: String)

    /**
     * Updates the question number on the view.
     *
     * @param qNumber   The current question number
     * @param tNumber   The total question number count of the question set
     */
    fun updateQuestionNumber(qNumber: String, tNumber: String)

    companion object {

        //View name
        val VIEW_NAME = "SELQuestion"

        //Arguments
        val ARG_QUESTION_TEXT = "argQuestionText"
        val ARG_QUESTION_INDEX = "argQuestionIndex"
        val ARG_QUESTION_TOTAL = "argQuestionTotal"
    }


}
