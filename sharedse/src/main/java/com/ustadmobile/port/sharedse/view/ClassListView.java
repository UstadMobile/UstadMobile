package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.port.sharedse.model.AttendanceClass;

/**
 * Created by varuna on 20/02/16.
 */
public interface ClassListView extends UstadView {

    /**
     * Sets the list of classes to be viewed here
     *
     * @param classList
     */
    void setClassList(final AttendanceClass[] classList);

    void setClassStatus(String classId, int statusCode, String statusMessage);


}
