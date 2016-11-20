package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.ClassManagementView2;

import java.util.Hashtable;

/**
 * Controller that really is just a shell to hold tabs
 *
 * Created by mike on 20/11/16.
 */

public class ClassManagementController2 extends UstadBaseController {

    public static final String ARG_CLASSID = "classid";

    public static final String ARG_CLASS_NAME = "classname";

    private String classId;

    public ClassManagementController2(Object context) {
        super(context);
    }

    @Override
    public void setUIStrings() {

    }

    public static ClassManagementController2 makeControllerForView(Hashtable args, ClassManagementView2 view) {
        ClassManagementController2 controller = new ClassManagementController2(view.getContext());
        controller.classId = args.get(ARG_CLASSID).toString();
        view.setClassName(args.get(ARG_CLASS_NAME).toString());
        return controller;
    }

    public String getClassId() {
        return classId;
    }

}
