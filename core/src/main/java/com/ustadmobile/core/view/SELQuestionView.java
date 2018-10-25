package com.ustadmobile.core.view;


/**
 * SELQuestion Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELQuestionView extends UstadView {

    String VIEW_NAME = "SELQuestion";

    String ARG_QUESTION_TEXT = "argQuestionText";
    String ARG_QUESTION_INDEX = "argQuestionIndex";
    String ARG_QUESTION_TOTAL = "argQuestionTotal";

    void finish();

    void updateQuestion(String questionText);
    void updateQuestionNumber(String qNumber, String tNumber);


}
