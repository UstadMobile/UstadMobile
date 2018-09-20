package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * ClazzListEnrollPerson Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzListEnrollPersonView extends UstadView {

    String VIEW_NAME = "ClazzListEnrollPerson";

    void setClazzListProvider(UmProvider<ClazzWithEnrollment> clazzEnrollmentListProvider);
    /**
     * This will close the activity (and finish it)
     */
    void finish();


}
