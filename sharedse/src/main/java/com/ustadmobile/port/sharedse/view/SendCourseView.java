package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.UstadView;

import java.util.List;

/**
 * Created by mike on 8/15/17.
 */

public interface SendCourseView extends UstadView, DismissableDialog {

    String VIEW_NAME = "SendCourse";

    void setReceivers(List<String> ids, List<String> names);

    void addReceiver(String id, String name);

    void clearReceivers();

    void removeReceiver(String id);

    /**
     * Sets whether or not the receivers are enabled. Once a user clicks on a receiver they are
     * disabled until the connection is successful or it fails.
     *
     * @param enabled
     */
    void setReceiversListEnabled(boolean enabled);

    /**
     * Set the status text e.g. Scanning, Connecting etc.
     *
     * @param statusText
     */
    void setStatusText(String statusText);

    void setReceiverStatus(String receiverId, int receiverStatus);

}
