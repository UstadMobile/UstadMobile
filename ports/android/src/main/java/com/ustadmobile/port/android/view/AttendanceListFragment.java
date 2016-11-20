package com.ustadmobile.port.android.view;

import android.os.Bundle;

import com.ustadmobile.core.controller.AttendanceListController;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;

/**
 * Created by mike on 20/11/16.
 */

public class AttendanceListFragment extends EntityListFragment implements ControllerReadyListener{

    private AttendanceListController mAttendanceListController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void controllerReady(UstadController controller, int flags) {
        mAttendanceListController = (AttendanceListController)controller;
    }
}
