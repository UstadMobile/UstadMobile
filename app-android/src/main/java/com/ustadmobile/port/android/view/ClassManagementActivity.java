package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.ClassManagementController;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;
import com.ustadmobile.port.sharedse.view.ClassManagementView;

public class ClassManagementActivity extends UstadBaseActivity implements ClassManagementView,
        View.OnClickListener {

    private ClassManagementController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_management);

        mController = ClassManagementController.makeControllerForView(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()));
        setBaseController(mController);
        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((Button)findViewById(R.id.class_management_attendance_button)).setOnClickListener(this);
        ((FloatingActionButton)findViewById(R.id.class_management_new_student_fab)
            ).setOnClickListener(this);

    }

    @Override
    public void setClassName(String className) {
        setTitle(className);
    }

    @Override
    public void setStudentList(AttendanceClassStudent[] students) {
        ((StudentListView)findViewById(R.id.class_management_studentlist)).setStudents(students);
    }

    @Override
    public void updateStudentList(final AttendanceClassStudent[] students){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //stuff that updates ui
                ((StudentListView)findViewById(R.id.class_management_studentlist)).setStudents(students);

            }
        });
    }

    @Override
    public void setAttendanceLabel(String attendanceLabel) {
        ((Button)findViewById(R.id.class_management_attendance_button)).setText(attendanceLabel);
    }

    @Override
    public void setExamsLabel(String examsLabel) {
        ((Button)findViewById(R.id.class_management_exams_button)).setText(examsLabel);
    }

    @Override
    public void setReportsLabel(String reportsLabel) {
        ((Button)findViewById(R.id.class_management_reports_button)).setText(reportsLabel);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.class_management_attendance_button) {
            mController.handleClickAttendanceButton();
        }else if(v.getId() == R.id.class_management_new_student_fab){
            mController.handleShowEnrollForm();
        }
    }
}
