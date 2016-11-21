package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AttendanceListView;
import com.ustadmobile.core.view.AttendanceView;

import java.util.Hashtable;

/**
 * Created by mike on 20/11/16.
 */

public class AttendanceListController extends EntityListController {

    public AttendanceListController(Object context) {
        super(context);
    }

    private String classId;

    public static void makeControllerForView(AttendanceListView view, Hashtable args, ControllerReadyListener listener) {
        AttendanceListController controller = new AttendanceListController(view.getContext());
        new LoadControllerThread(args, controller, listener, view).start();
    }

    @Override
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        String classId = args.get(ClassManagementController2.ARG_CLASSID).toString();
        String attendanceActivityId = UstadMobileConstants.PREFIX_ATTENDANCE_URL + classId;

        AttendanceListController controller = new AttendanceListController(context);
        controller.setClassId(classId);
        return controller;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void handleClickSnapSheet() {
        Hashtable args = new Hashtable();
        args.put(AttendanceController.KEY_CLASSID, classId);
        UstadMobileSystemImpl.getInstance().go(AttendanceView.class, args, context);
    }

}
