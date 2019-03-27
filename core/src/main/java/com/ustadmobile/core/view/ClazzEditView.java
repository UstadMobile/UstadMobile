package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Schedule;

/**
 * ClazzEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzEditView extends UstadView {

    String VIEW_NAME = "ClazzEdit";

    String ARG_NEW = "ArgNew";

    String ARG_SCHEDULE_UID = "argScheduleUid";

    /**
     * For Android: closes the activity.
     */
    void finish();

    void updateToolbarTitle(String titleName);

    /**
     * Provider for schedule of this class.
     *
     * @param clazzScheduleProvider The Provider of Schedule type
     */
    void setClazzScheduleProvider(UmProvider<Schedule> clazzScheduleProvider);

    void updateClazzEditView(Clazz updatedClazz );

    void setHolidayPresets(String[] presets, int position);

    /**
     * Handles holiday selected
     * @param id    The id/position of the Holiday selected from the drop-down.
     */
    void setHolidaySelected(long id);

}
