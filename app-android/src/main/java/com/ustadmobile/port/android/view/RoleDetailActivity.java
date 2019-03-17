package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.RoleDetailPresenter;
import com.ustadmobile.core.view.RoleDetailView;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class RoleDetailActivity extends UstadBaseActivity implements RoleDetailView {

    private Toolbar toolbar;
    private RoleDetailPresenter mPresenter;
    private EditText title;

    private CheckBox viewPeople, addPeople, updatePeople, viewPP, addPP, updatePP, viewClasses,
            addClasses, updateClasses, addTeachersToClass, addStudentsToClass, viewAttendance,
    takeAttendance, updateAttendance, viewClassActivity, takeClassActivity, updateClassActivity,
    viewSELQuestions, addSELQuestions, updateSELQuestions, viewSELresults, recordSEL, updateSEL;

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_role_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_role_detail_toolbar);
        toolbar.setTitle(getText(R.string.new_role));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewPeople = findViewById(R.id.permission_person_select);
        addPeople = findViewById(R.id.permission_person_insert);
        updatePeople = findViewById(R.id.permission_person_update);
        viewPP = findViewById(R.id.permission_person_picture_select);
        addPP = findViewById(R.id.permission_person_picture_insert);
        updatePP = findViewById(R.id.permission_person_picture_update);
        viewClasses = findViewById(R.id.permission_clazz_select);
        addClasses = findViewById(R.id.permission_clazz_insert);
        updateClasses = findViewById(R.id.permission_clazz_update);
        addTeachersToClass = findViewById(R.id.permission_clazz_add_teacher);
        addStudentsToClass = findViewById(R.id.permission_clazz_add_student);
        viewAttendance = findViewById(R.id.permission_attendance_select);
        takeAttendance = findViewById(R.id.permission_attendance_insert);
        updateAttendance = findViewById(R.id.permission_attendance_update);
        viewClassActivity = findViewById(R.id.permission_activity_select);
        takeClassActivity = findViewById(R.id.permission_activity_insert);
        updateClassActivity = findViewById(R.id.permission_activity_update);
        viewSELQuestions = findViewById(R.id.permission_sel_question_select);
        addSELQuestions = findViewById(R.id.permission_sel_question_insert);
        updateSELQuestions = findViewById(R.id.permission_sel_question_update);
        viewSELresults = findViewById(R.id.permission_sel_select);
        recordSEL = findViewById(R.id.permission_sel_insert);
        updateSEL = findViewById(R.id.permission_sel_update);


        title = findViewById(R.id.activity_role_detail_name);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.updateRoleName(s.toString());
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Call the Presenter
        mPresenter = new RoleDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_role_detail_fab);

        fab.setOnClickListener(v -> {
            mPresenter.setPermissionField(calcualtePermissionFromView());
            mPresenter.handleClickDone();
        });
    }

    public long calcualtePermissionFromView(){
        long permission =
                (viewPeople.isChecked()?Role.PERMISSION_PERSON_SELECT:0) |
                (addPeople.isChecked()?Role.PERMISSION_PERSON_INSERT:0) |
                (updatePeople.isChecked()?Role.PERMISSION_PERSON_UPDATE:0) |
                (viewPP.isChecked()?Role.PERMISSION_PERSON_PICTURE_SELECT:0) |
                (addPP.isChecked()?Role.PERMISSION_PERSON_PICTURE_INSERT:0) |
                (updatePP.isChecked()?Role.PERMISSION_PERSON_PICTURE_UPDATE:0) |
                (viewClasses.isChecked()?Role.PERMISSION_CLAZZ_SELECT:0) |
                (addClasses.isChecked()?Role.PERMISSION_CLAZZ_INSERT:0) |
                (updateClasses.isChecked()?Role.PERMISSION_CLAZZ_UPDATE:0) |
                (addTeachersToClass.isChecked()?Role.PERMISSION_CLAZZ_ADD_TEACHER:0) |
                (addStudentsToClass.isChecked()?Role.PERMISSION_CLAZZ_ADD_STUDENT:0) |
                (viewAttendance.isChecked()?Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT:0) |
                (takeAttendance.isChecked()?Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT:0) |
                (updateAttendance.isChecked()?Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE:0) |
                (viewClassActivity.isChecked()?Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT:0) |
                (takeClassActivity.isChecked()?Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT:0) |
                (updateClassActivity.isChecked()?Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE:0) |
                (viewSELQuestions.isChecked()?Role.PERMISSION_SEL_QUESTION_SELECT:0) |
                (addSELQuestions.isChecked()?Role.PERMISSION_SEL_QUESTION_INSERT:0) |
                (updateSELQuestions.isChecked()?Role.PERMISSION_SEL_QUESTION_UPDATE:0) |
                (viewSELresults.isChecked()?Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT:0) |
                (recordSEL.isChecked()?Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT:0) |
                (updateSEL.isChecked()?Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE:0);

        return permission;
    }

    public void updateCheckBoxes(long permission){
        viewPeople.setChecked((permission & Role.PERMISSION_PERSON_SELECT) > 0);
        addPeople.setChecked((permission&Role.PERMISSION_PERSON_INSERT) > 0);
        updatePeople.setChecked((permission&Role.PERMISSION_PERSON_UPDATE) > 0);
        viewPP.setChecked((permission&Role.PERMISSION_PERSON_PICTURE_SELECT) > 0);
        addPP.setChecked((permission&Role.PERMISSION_PERSON_PICTURE_INSERT) > 0);
        updatePP.setChecked((permission&Role.PERMISSION_PERSON_PICTURE_UPDATE) > 0);
        viewClasses.setChecked((permission&Role.PERMISSION_CLAZZ_SELECT) > 0);
        addClasses.setChecked((permission&Role.PERMISSION_CLAZZ_INSERT) > 0);
        updateClasses.setChecked((permission&Role.PERMISSION_CLAZZ_UPDATE) > 0);
        addTeachersToClass.setChecked((permission&Role.PERMISSION_CLAZZ_ADD_TEACHER) > 0);
        addStudentsToClass.setChecked((permission&Role.PERMISSION_CLAZZ_ADD_STUDENT) > 0);
        viewAttendance.setChecked((permission&Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT) > 0);
        takeAttendance.setChecked((permission&Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT) > 0);
        updateAttendance.setChecked((permission&Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE) > 0);
        viewClassActivity.setChecked((permission&Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT) > 0);
        takeClassActivity.setChecked((permission&Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT) > 0);
        updateClassActivity.setChecked((permission&Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE) > 0);
        viewSELQuestions.setChecked((permission&Role.PERMISSION_SEL_QUESTION_SELECT) > 0);
        addSELQuestions.setChecked((permission&Role.PERMISSION_SEL_QUESTION_INSERT) > 0);
        updateSELQuestions.setChecked((permission&Role.PERMISSION_SEL_QUESTION_UPDATE) > 0);
        viewSELresults.setChecked((permission&Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT) > 0);
        recordSEL.setChecked((permission&Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT) > 0);
        updateSEL.setChecked((permission&Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE) > 0);
    }

    @Override
    public void updateRoleOnView(Role role) {

        String roleName = "";


        if(role != null){
            if(role.getRoleName() != null){
                roleName = role.getRoleName();

            }
        }

        String finalRoleName = roleName;
        runOnUiThread(() -> {
            if (!finalRoleName.isEmpty()){
                toolbar.setTitle(finalRoleName);
            }
            title.setText(finalRoleName);
            //title.setFocusable(false);
            updateCheckBoxes(role.getRolePermissions());
        });


    }
}
