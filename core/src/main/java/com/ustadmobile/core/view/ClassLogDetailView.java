package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;

/**
 * ClassLogDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClassLogDetailView extends UstadView {

    //The View name
    String VIEW_NAME = "ClassLogDetail";

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set ot the Recycler View.
     *
     * @param clazzLogAttendanceRecordProvider The provider data
     */
    void setClazzLogAttendanceRecordProvider(UmProvider<ClazzLogAttendanceRecordWithPerson>
                                                     clazzLogAttendanceRecordProvider);

    /**
     * Finish the view (close it)
     */
    void finish();

    /**
     * Update the title bar of the Take Attendance ClazzLogDetail Activity.
     *
     * @param title The title of the ClazzLogDetailActivity string
     */
    void updateToolbarTitle(String title);

    /**
     * Updates the date heading in the activity
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    void updateDateHeading(String dateString);

    void showMarkAllButtons(boolean show);

}
