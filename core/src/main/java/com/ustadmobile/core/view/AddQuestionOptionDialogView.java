package com.ustadmobile.core.view;

public interface AddQuestionOptionDialogView extends UstadView{
    String VIEW_NAME="AddQuestionOptionDialogView";
    String ARG_QUESTION_OPTION_UID = "ArgQuestionOptionUid";
    void finish();
    void setOptionText(String text);
}
