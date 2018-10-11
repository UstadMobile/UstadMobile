package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

/**
 * SELSelectStudentView Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SELSelectStudentView extends UstadView {

    String VIEW_NAME = "SELSelectStudent";

    /**
     * Sets Current SEL answers by students list
     *
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param selStudentsProvider The provider data
     */
    void setSELAnswerListProvider(UmProvider<Person> selStudentsProvider);

    void finish();

}
