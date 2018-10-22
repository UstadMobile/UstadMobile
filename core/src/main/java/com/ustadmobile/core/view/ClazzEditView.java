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

    /**
     * For Android: closes the activity.
     */
    void finish();

    void updateToolbarTitle(String titleName);

    /**
     * Provider for schedule of this class.
     * @param clazzScheduleProvider
     */
    void setClazzScheduleProvider(UmProvider<Schedule> clazzScheduleProvider);

    void updateClazzEditView(Clazz updatedClazz );

}
