package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;

/**
 * View for editing an SEL nomination. SELEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELEditView extends UstadView {

    //View name
    String VIEW_NAME = "SELEdit";

    //Arguments
    String ARG_QUESTION_SET_UID = "questionSetUid";
    String ARG_QUESTION_UID = "questionUid";
    String ARG_CLAZZMEMBER_UID = "clazzMemberUid";
    String ARG_QUESTION_INDEX_ID = "questionIndexId";
    String ARG_QUESTION_SET_RESPONSE_UID = "questionSetResponseUid";
    String ARG_QUESTION_RESPONSE_UID = "questionResponseUid";

    /**
     * Sets Current provider This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<PersonWithPersonPicture> listProvider);

    /**
     * Updates the heading on the SEL Edit page. Usually the question text.
     *
     * @param questionText      The question text.
     */
    void updateHeading(String questionText);

    /**
     * Updates the question count on the SEL Edit page. Usually the question counter/total
     *
     * @param iNum      The current question number
     * @param tNum      The total questions in the question set
     */
    void updateHeading(String iNum, String tNum);

    /**
     * Closes the view.
     */
    void finish();

}
