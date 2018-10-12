package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.core.db.UmProvider;

/**
 * SELEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELEditView extends UstadView {

    String VIEW_NAME = "SELEdit";

    String ARG_QUESTION_SET_UID = "questionSetUid";

    String ARG_QUESTION_UID = "questionUid";

    String ARG_CLAZZMEMBER_UID = "clazzMemberUid";

    String ARG_QUESTION_INDEX_ID = "questionIndexId";

    String ARG_QUESTION_SET_RESPONSE_UID = "questionSetResponseUid";

    String ARG_QUESTION_RESPONSE_UID = "questionResponseUid";


    /**
     * Sets Current provider
     * <p>
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<Person> listProvider);

    void finish();


}
