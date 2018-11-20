package com.ustadmobile.core.view;


/**
 * View represents editing SEL Questions that will be asked as per the SEL tasks.
 * SELQuestionEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELQuestionEditView extends UstadView {

    //View name
    String VIEW_NAME = "SELQuestionEdit";

    /**
     * Closes the view.
     */
    void finish();


}
