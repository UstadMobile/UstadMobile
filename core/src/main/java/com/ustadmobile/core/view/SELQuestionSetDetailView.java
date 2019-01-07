package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;

public interface SELQuestionSetDetailView extends UstadView {
    String VIEW_NAME="SELQuestionSetDetailView";
    String ARG_SEL_QUESTION_SET_UID = "SELQuestionSetUid";
    String ARG_SEL_QUESTION_SET_NAME = "SELQuestionSetName";
    void finish();
    void setListProvider(UmProvider<SocialNominationQuestion> listProvider);
    void updateToolbarTitle(String title);
}
