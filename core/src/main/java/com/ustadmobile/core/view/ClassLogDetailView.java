package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;

/**
 * ClassLogDetail Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClassLogDetailView extends UstadView {

    String VIEW_NAME = "ClassLogDetail";

    /**
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set ot the Recycler View.
     *
     * @param clazzLogAttendanceRecordProvider The provider data
     */
    void setClazzLogAttendanceRecordProvider(UmProvider<ClazzLogAttendanceRecordWithPerson>
                                                     clazzLogAttendanceRecordProvider);

}
