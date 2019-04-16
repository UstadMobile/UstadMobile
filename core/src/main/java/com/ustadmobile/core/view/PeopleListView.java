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

    int SORT_ORDER_NAME_ASC = 1;
    int SORT_ORDER_NAME_DESC = 2;
    int SORT_ORDER_ATTENDANCE_ASC = 3;
    int SORT_ORDER_ATTENDANCE_DESC = 4;

    /**
     * Set people list provider to the view.
     *
     * @param listProvider  The people list of PersonWithEnrollment type
     */
    void setPeopleListProvider(UmProvider<PersonWithEnrollment> listProvider);

    void showFAB(boolean show);

    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    void updateSortSpinner(String[] presets);
}
