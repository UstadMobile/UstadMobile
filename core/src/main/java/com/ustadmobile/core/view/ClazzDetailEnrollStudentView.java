package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

/**
 * ClazzDetailEnrollStudentView Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzDetailEnrollStudentView extends UstadView {

    //The View name
    String VIEW_NAME = "ClazzDetailEnrollStudent";

    // ARGUMENT TO VIEWS THAT DENOTES that this is a new person.
    String ARG_NEW_PERSON = "argNewPerson";

    /**
     * Method to set the Person provider.
     *
     * @param studentsProvider The provider of type PersonWithEnrollment
     */
    void setStudentsProvider(UmProvider<PersonWithEnrollment> studentsProvider);

    /**
     * Finish activity: Close it.
     */
    void finish();

}
