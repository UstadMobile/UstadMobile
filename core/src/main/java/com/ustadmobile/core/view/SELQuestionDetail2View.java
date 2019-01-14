package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionOption;

public interface SELQuestionDetail2View extends UstadView {
    String VIEW_NAME="SELQuestionDetail2";
    String ARG_QUESTION_UID_QUESTION_DETAIL = "ARGQuestionUidForQuestionDetail";
    String ARG_QUESTION_OPTION_UID = "ArgQuestionOptionUid";
    void setQuestionOptionsProvider(UmProvider<SocialNominationQuestionOption> listProvider);
    void setQuestionTypePresets(String[] presets);
    void finish();
    void setQuestionText(String questionText);
    void setQuestionType(int type);
    void handleClickDone();
    void showQuestionOptions(boolean show);
    void handleQuestionTypeChange(int type);
    void handleClickAddOption();
    void setQuestionTypeListener();
    void setQuestionOnView(SocialNominationQuestion selQuestion);
}
