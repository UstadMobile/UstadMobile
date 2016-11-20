package com.ustadmobile.core.controller;

import com.ustadmobile.core.model.AttendanceClassStudent;
import com.ustadmobile.core.model.ListableEntity;
import com.ustadmobile.core.view.PersonListView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by mike on 20/11/16.
 */

public class PersonListController extends EntityListController{


    public PersonListController(Object context) {
        super(context);

    }

    @Override
    public void setUIStrings() {

    }

    public void loadStudentsByClassId(String classId) {
        AttendanceClassStudent[] students = AttendanceController.loadClassStudentListFromPrefs(
            classId, context);
        for(int i = 0; i < students.length; i++) {
            entityList.add(students[i]);
        }
    }


    @Override
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        PersonListController controller = new PersonListController(context);
        controller.loadStudentsByClassId(args.get(ClassManagementController2.ARG_CLASSID).toString());
        return controller;
    }

    public static void makeControllerForView(PersonListView view, Hashtable args, ControllerReadyListener readyListener) {
        PersonListController controller = new PersonListController(view.getContext());
        new LoadControllerThread(args, controller, readyListener, view).start();
    }

}
