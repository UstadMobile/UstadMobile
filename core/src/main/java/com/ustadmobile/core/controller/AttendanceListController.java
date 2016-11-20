package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.AttendanceListView;

import java.util.Hashtable;

/**
 * Created by mike on 20/11/16.
 */

public class AttendanceListController extends EntityListController {

    public AttendanceListController(Object context) {
        super(context);
    }

    public static void makeControllerForView(AttendanceListView view, Hashtable args, ControllerReadyListener listener) {
        AttendanceListController controller = new AttendanceListController(view.getContext());
        new LoadControllerThread(args, controller, listener, view).start();
    }

    @Override
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        return new AttendanceListController(context);
    }
}
