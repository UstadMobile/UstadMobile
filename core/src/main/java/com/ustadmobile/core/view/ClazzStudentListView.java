package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

/**
 * ClassStudentList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzStudentListView extends UstadView {

    String VIEW_NAME = "ClassStudentList";

    /**
     * This methods purpose is to set the provider given to it to the view.
     * On Android it will be set to the Recycler View
     *
     * @param setPersonUmProvider  The provider data
     */
    void setPersonWithEnrollmentProvider(UmProvider<PersonWithEnrollment>
                                         setPersonUmProvider);


    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class Student list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    void updateSortSpinner(String[] presets);

}
