package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClassManagementController;
import com.ustadmobile.core.model.AttendanceClassStudent;
import com.ustadmobile.core.view.ClassManagementView;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import android.widget.Button;
import android.widget.AdapterView;

public class ClassManagementActivity extends UstadBaseActivity implements ClassManagementView, View.OnClickListener {

    private ClassManagementController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_management);

        mController = ClassManagementController.makeControllerForView(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()));
        setBaseController(mController);
        setUMToolbar();
        ((Button)findViewById(R.id.class_management_attendance_button)).setOnClickListener(this);

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
        switch(v.getId()) {
            case R.id.class_management_attendance_button:
                mController.handleClickAttendanceButton();
                break;
        }
    }
}
