package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

/**
 * Created by mike on 20/11/16.
 */

public interface ClassManagementView2 extends UstadView {

    public static final String VIEW_NAME = "ClassManagement2";

    /**
     * Sets the name of the class (should be the title normally)
     *
     * @param className
     */
    void setClassName(String className);
    
}
