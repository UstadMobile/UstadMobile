package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.core.db.UmProvider;

/**
 * View responsible for showing a list of SEL questions in the question set for SEL task.
 * SELQuestionDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELQuestionDetailView extends UstadView {

    //View name
    String VIEW_NAME = "SELQuestionDetail";

    /**
     * Sets Current provider This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<SocialNominationQuestion> listProvider);

    /**
     * Closes the view.
     */
    void finish();


}
