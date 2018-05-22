package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

/**
 * Created by mike on 8/22/17.
 */

public interface ReceiveCourseView extends UstadView {

    String VIEW_NAME = "ReceiveCourse";

    /**
     * The view shows waiting for sender with a progress view
     */
    int MODE_WAITING = 0;

    /**
     * The view shows the title of what was sent and provides an option to accept or decline
     */
    int MODE_ACCEPT_DECLINE = 1;

    /**
     * The view shows a message that a connection is established but the gruop owner is not sharing
     * anything
     */
    int MODE_CONNECTED_BUT_NOT_SHARING = 2;

    void setMode(int mode);

    void setWaitingStatusText(int messageCode);

    void setSharedCourseName(String courseName);

    void setSenderName(String senderName);

    void setButtonsEnabled(boolean visible);

    void setDeviceName(String deviceName);

}
