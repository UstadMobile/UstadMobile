package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;

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
     * @param clazzStudentsWithAttendanceUmProvider  The provider data
     */
    void setClazzMembersProvider(UmProvider<ClazzMemberWithPerson>
                                                          clazzStudentsWithAttendanceUmProvider);

}
