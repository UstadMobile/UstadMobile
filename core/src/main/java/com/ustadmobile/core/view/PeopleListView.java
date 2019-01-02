package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

/**
 * PeopleList is the core view responsible for showing all people in a list.
 * PeopleList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface PeopleListView extends UstadView {

    String VIEW_NAME = "PeopleList";

    /**
     * Set people list provider to the view.
     *
     * @param listProvider  The people list of PersonWithEnrollment type
     */
    void setPeopleListProvider(UmProvider<PersonWithEnrollment> listProvider);

    void showFAB(boolean show);
}
