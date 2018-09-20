package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

/**
 * ClazzDetailEnrollStudentView Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzDetailEnrollStudentView extends UstadView {

    String VIEW_NAME = "ClazzDetailEnrollStudent";

    void setStudentsProvider(UmProvider<PersonWithEnrollment> studentsProvider);

    void finish();

}
