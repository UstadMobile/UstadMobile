package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.RoleDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.RoleDetailView
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton

class RoleDetailActivity : UstadBaseActivity(), RoleDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: RoleDetailPresenter? = null
    private var title: EditText? = null

    private var viewPeople: CheckBox? = null
    private var addPeople: CheckBox? = null
    private var updatePeople: CheckBox? = null
    private var viewPP: CheckBox? = null
    private var addPP: CheckBox? = null
    private var updatePP: CheckBox? = null
    private var viewClasses: CheckBox? = null
    private var addClasses: CheckBox? = null
    private var updateClasses: CheckBox? = null
    private var addTeachersToClass: CheckBox? = null
    private var addStudentsToClass: CheckBox? = null
    private var viewAttendance: CheckBox? = null
    private var takeAttendance: CheckBox? = null
    private var updateAttendance: CheckBox? = null
    private var viewClassActivity: CheckBox? = null
    private var takeClassActivity: CheckBox? = null
    private var updateClassActivity: CheckBox? = null
    private var viewSELQuestions: CheckBox? = null
    private var addSELQuestions: CheckBox? = null
    private var updateSELQuestions: CheckBox? = null
    private var viewSELresults: CheckBox? = null
    private var recordSEL: CheckBox? = null
    private var updateSEL: CheckBox? = null

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_role_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_role_detail_toolbar)
        toolbar!!.title = getText(R.string.new_role)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        viewPeople = findViewById(R.id.permission_person_select)
        addPeople = findViewById(R.id.permission_person_insert)
        updatePeople = findViewById(R.id.permission_person_update)
        viewPP = findViewById(R.id.permission_person_picture_select)
        addPP = findViewById(R.id.permission_person_picture_insert)
        updatePP = findViewById(R.id.permission_person_picture_update)
        viewClasses = findViewById(R.id.permission_clazz_select)
        addClasses = findViewById(R.id.permission_clazz_insert)
        updateClasses = findViewById(R.id.permission_clazz_update)
        addTeachersToClass = findViewById(R.id.permission_clazz_add_teacher)
        addStudentsToClass = findViewById(R.id.permission_clazz_add_student)
        viewAttendance = findViewById(R.id.permission_attendance_select)
        takeAttendance = findViewById(R.id.permission_attendance_insert)
        updateAttendance = findViewById(R.id.permission_attendance_update)
        viewClassActivity = findViewById(R.id.permission_activity_select)
        takeClassActivity = findViewById(R.id.permission_activity_insert)
        updateClassActivity = findViewById(R.id.permission_activity_update)
        viewSELQuestions = findViewById(R.id.permission_sel_question_select)
        addSELQuestions = findViewById(R.id.permission_sel_question_insert)
        updateSELQuestions = findViewById(R.id.permission_sel_question_update)
        viewSELresults = findViewById(R.id.permission_sel_select)
        recordSEL = findViewById(R.id.permission_sel_insert)
        updateSEL = findViewById(R.id.permission_sel_update)


        title = findViewById(R.id.activity_role_detail_name)
        title!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateRoleName(s.toString())
            }
        })

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //Call the Presenter
        mPresenter = RoleDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_role_detail_fab)

        fab.setOnClickListener { v ->
            mPresenter!!.permissionField = calcualtePermissionFromView()
            mPresenter!!.handleClickDone()
        }
    }

    fun calcualtePermissionFromView(): Long {

        return (if (viewPeople!!.isChecked) Role.PERMISSION_PERSON_SELECT else 0) or
                (if (addPeople!!.isChecked) Role.PERMISSION_PERSON_INSERT else 0) or
                (if (updatePeople!!.isChecked) Role.PERMISSION_PERSON_UPDATE else 0) or
                (if (viewPP!!.isChecked) Role.PERMISSION_PERSON_PICTURE_SELECT else 0) or
                (if (addPP!!.isChecked) Role.PERMISSION_PERSON_PICTURE_INSERT else 0) or
                (if (updatePP!!.isChecked) Role.PERMISSION_PERSON_PICTURE_UPDATE else 0) or
                (if (viewClasses!!.isChecked) Role.PERMISSION_CLAZZ_SELECT else 0) or
                (if (addClasses!!.isChecked) Role.PERMISSION_CLAZZ_INSERT else 0) or
                (if (updateClasses!!.isChecked) Role.PERMISSION_CLAZZ_UPDATE else 0) or
                (if (addTeachersToClass!!.isChecked) Role.PERMISSION_CLAZZ_ADD_TEACHER else 0) or
                (if (addStudentsToClass!!.isChecked) Role.PERMISSION_CLAZZ_ADD_STUDENT else 0) or
                (if (viewAttendance!!.isChecked) Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT else 0) or
                (if (takeAttendance!!.isChecked) Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT else 0) or
                (if (updateAttendance!!.isChecked) Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE else 0) or
                (if (viewClassActivity!!.isChecked) Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT else 0) or
                (if (takeClassActivity!!.isChecked) Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT else 0) or
                (if (updateClassActivity!!.isChecked) Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE else 0) or
                (if (viewSELQuestions!!.isChecked) Role.PERMISSION_SEL_QUESTION_SELECT else 0) or
                (if (addSELQuestions!!.isChecked) Role.PERMISSION_SEL_QUESTION_INSERT else 0) or
                (if (updateSELQuestions!!.isChecked) Role.PERMISSION_SEL_QUESTION_UPDATE else 0) or
                (if (viewSELresults!!.isChecked) Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT else 0) or
                (if (recordSEL!!.isChecked) Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT else 0) or
                if (updateSEL!!.isChecked) Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE else 0
    }

    fun updateCheckBoxes(permission: Long) {
        viewPeople!!.isChecked = permission and Role.PERMISSION_PERSON_SELECT > 0
        addPeople!!.isChecked = permission and Role.PERMISSION_PERSON_INSERT > 0
        updatePeople!!.isChecked = permission and Role.PERMISSION_PERSON_UPDATE > 0
        viewPP!!.isChecked = permission and Role.PERMISSION_PERSON_PICTURE_SELECT > 0
        addPP!!.isChecked = permission and Role.PERMISSION_PERSON_PICTURE_INSERT > 0
        updatePP!!.isChecked = permission and Role.PERMISSION_PERSON_PICTURE_UPDATE > 0
        viewClasses!!.isChecked = permission and Role.PERMISSION_CLAZZ_SELECT > 0
        addClasses!!.isChecked = permission and Role.PERMISSION_CLAZZ_INSERT > 0
        updateClasses!!.isChecked = permission and Role.PERMISSION_CLAZZ_UPDATE > 0
        addTeachersToClass!!.isChecked = permission and Role.PERMISSION_CLAZZ_ADD_TEACHER > 0
        addStudentsToClass!!.isChecked = permission and Role.PERMISSION_CLAZZ_ADD_STUDENT > 0
        viewAttendance!!.isChecked = permission and Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT > 0
        takeAttendance!!.isChecked = permission and Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT > 0
        updateAttendance!!.isChecked = permission and Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE > 0
        viewClassActivity!!.isChecked = permission and Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT > 0
        takeClassActivity!!.isChecked = permission and Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT > 0
        updateClassActivity!!.isChecked = permission and Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE > 0
        viewSELQuestions!!.isChecked = permission and Role.PERMISSION_SEL_QUESTION_SELECT > 0
        addSELQuestions!!.isChecked = permission and Role.PERMISSION_SEL_QUESTION_INSERT > 0
        updateSELQuestions!!.isChecked = permission and Role.PERMISSION_SEL_QUESTION_UPDATE > 0
        viewSELresults!!.isChecked = permission and Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT > 0
        recordSEL!!.isChecked = permission and Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT > 0
        updateSEL!!.isChecked = permission and Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE > 0
    }

    override fun updateRoleOnView(role: Role) {

        var roleName: String? = ""


        if (role != null) {
            if (role.roleName != null) {
                roleName = role.roleName

            }
        }

        val finalRoleName = roleName
        runOnUiThread {
            if (!finalRoleName!!.isEmpty()) {
                toolbar!!.title = finalRoleName
            }
            title!!.setText(finalRoleName)
            //title.setFocusable(false);
            updateCheckBoxes(role.rolePermissions)
        }


    }
}
