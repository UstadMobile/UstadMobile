package com.ustadmobile.port.android.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AttendanceController;
import com.ustadmobile.core.model.AttendanceRowModel;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.port.android.util.UMAndroidUtil;


public class AttendanceActivity extends UstadBaseActivity implements AttendanceView {

    protected AttendanceController mController;

    public static final String TAG_CAMERAFRAG= "camerafrag";

    public static final String TAG_RESULTSFRAG = "resultsfrag";

    protected AttendanceRowModel[] mAttendanceResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        mController = AttendanceController.makeControllerForView(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()));
        setBaseController(mController);
        setUMToolbar();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Attendance");
        if(savedInstanceState == null) {
            mController.handleStartFlow();
        }
    }

    @Override
    public void goBack() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void showTakePicture() {
        Fragment cameraFragment = AttendanceCameraFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.attendance_fragment_container,
                cameraFragment, TAG_CAMERAFRAG).commit();
    }

    @Override
    public void showResult(AttendanceRowModel[] results) {
        this.mAttendanceResults = results;
        Fragment resultsFrag = AttendanceConfirmFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.attendance_fragment_container,
                resultsFrag, TAG_RESULTSFRAG).addToBackStack(TAG_CAMERAFRAG).commit();
    }

    protected AttendanceRowModel[] getAttendanceResults() {
        if(this.mAttendanceResults != null) {
            return mAttendanceResults;
        }else {
            return new AttendanceRowModel[0];
        }
    }

}
