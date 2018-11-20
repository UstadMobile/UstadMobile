package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

/**
 * SEL Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELAnswerListView extends UstadView {

    //View name
    String VIEW_NAME = "SELAnswerList";

    /**
     * Sets Current SEL answers by students list
     *
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param selAnswersProvider The provider data
     */
    void setSELAnswerListProvider(UmProvider<Person> selAnswersProvider);

}
