package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;

public interface SELQuestionSetsView extends UstadView {
    String VIEW_NAME="SELQuestionSets";
    String ARG_SEL_QUESTION_SET_UID = "ArgSELQuestionSetUid";
    void finish();
    void setListProvider(UmProvider<SocialNominationQuestionSet> listProvider);
}
