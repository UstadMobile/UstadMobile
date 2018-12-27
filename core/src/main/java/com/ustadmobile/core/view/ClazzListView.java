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
    String ARG_LOGDATE = "logdate";
    String ARG_CLAZZ_UID = "ClazzUid";

    int SORT_ORDER_NAME_ASC = 1;
    int SORT_ORDER_NAME_DESC = 2;
    int SORT_ORDER_ATTENDANCE_ASC = 3;
    int SORT_ORDER_ATTENDANCE_DESC = 4;
    int SORT_ORDER_TEACHER_ASC = 5;

    /**
     * Sets the class list as provider to the view.
     * @param clazzListProvider The UMProvider provider of ClazzWithNumStudents Type.
     */
    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider);

    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    void updateSortSpinner(String[] presets);


    void showAddClassButton(boolean show);


    void showAllClazzSettingsButton(boolean show);


}
