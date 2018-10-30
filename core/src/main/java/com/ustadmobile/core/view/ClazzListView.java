package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * ClassList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzListView extends UstadView {

    String VIEW_NAME = "ClassList";

    String ARGS_CLAZZLOG_UID = "clazzloguid";
    String TAG_STATUS = "status";
    String ARG_LOGDATE = "logdate";

    int SORT_ORDER_NAME_ASC = 1;
    int SORT_ORDER_NAME_DESC = 2;
    int SORT_ORDER_ATTENDANCE_ASC = 3;
    int SORT_ORDER_ATTENDANCE_DESC = 4;
    int SORT_ORDER_TEACHER_ASC = 5;

    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider);

    void updateSortSpinner(String[] presets);

}
