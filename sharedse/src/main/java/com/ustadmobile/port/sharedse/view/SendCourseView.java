package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

import java.util.List;

/**
 * Created by mike on 8/15/17.
 */

public interface SendCourseView extends UstadView {

    String VIEW_NAME = "SendCourse";

    void setReceivers(List<String> ids, List<String> names);

    void addReceiver(String id, String name);

    void clearReceivers();

    void removeReceiver(String id);

}
