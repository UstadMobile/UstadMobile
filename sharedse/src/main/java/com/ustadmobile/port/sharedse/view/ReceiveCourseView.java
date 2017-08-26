package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

/**
 * Created by mike on 8/22/17.
 */

public interface ReceiveCourseView extends UstadView {

    String VIEW_NAME = "ReceiveCourse";

    int MODE_WAITING = 0;

    int MODE_ACCEPT_DECLINE = 1;

    void setMode(int mode);

    void setWaitingStatusText(int messageCode);

    void setSharedCourseName(String courseName);

    void setSenderName(String senderName);

    void setButtonsEnabled(boolean visible);

    void setDeviceName(String deviceName);

}
