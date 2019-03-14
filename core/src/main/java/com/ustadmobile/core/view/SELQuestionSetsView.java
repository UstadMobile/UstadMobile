package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions;

/**
 * Core View. Screen is for SEL Question list.
 */
public interface SELQuestionSetsView extends UstadView {
    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME="SELQuestionSets";

    //Any argument keys:
    String ARG_SEL_QUESTION_SET_UID = "ArgSELQuestionSetUid";

    /**
     * Method to finish the screen / view.
     */
    void finish();

    /**
     * Sets the given provider to the view's provider adapter.
     * @param listProvider  The provider to set to the view
     */
    void setListProvider(UmProvider<SELQuestionSetWithNumQuestions> listProvider);
}
