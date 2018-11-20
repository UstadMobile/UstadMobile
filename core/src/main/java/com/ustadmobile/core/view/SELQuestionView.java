package com.ustadmobile.core.view;


/**
 * View represents a Question view between SEL nominations.
 * SELQuestion Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELQuestionView extends UstadView {

    //View name
    String VIEW_NAME = "SELQuestion";

    //Arguments
    String ARG_QUESTION_TEXT = "argQuestionText";
    String ARG_QUESTION_INDEX = "argQuestionIndex";
    String ARG_QUESTION_TOTAL = "argQuestionTotal";

    /**
     * Closes the view.
     */
    void finish();

    /**
     * Updates the question on the view's toolbar
     *
     * @param questionText  The question text
     */
    void updateQuestion(String questionText);

    /**
     * Updates the question number on the view.
     *
     * @param qNumber   The current question number
     * @param tNumber   The total question number count of the question set
     */
    void updateQuestionNumber(String qNumber, String tNumber);


}
