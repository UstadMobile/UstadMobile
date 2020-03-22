package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SettingsView
import com.ustadmobile.port.android.view.UstadBaseActivity

class SettingsActivity : UstadBaseActivity(), SettingsView {

    internal lateinit var mPresenter: SettingsPresenter
    internal lateinit var selLayout: ConstraintLayout
    internal lateinit var calendatLayout: ConstraintLayout
    internal lateinit var rolesLayout: ConstraintLayout
    internal lateinit var groupsLayout: ConstraintLayout
    internal lateinit var rolesAssignmentLayout: ConstraintLayout
    internal lateinit var locationsLayout: ConstraintLayout
    internal lateinit var auditLogLayout: ConstraintLayout
    internal lateinit var customFieldsLayout: ConstraintLayout
    internal lateinit var userCL: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting layout:
        setContentView(R.layout.activity_settings2)

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.activity_settings2_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        selLayout = findViewById(R.id.activity_settings2_sel_question_set_cl)

        calendatLayout = findViewById(R.id.activity_settings2_holiday_calendar_cl)
        rolesLayout = findViewById(R.id.activity_settings2_roles_cl)
        groupsLayout = findViewById(R.id.activity_settings2_groups_cl)
        rolesAssignmentLayout = findViewById(R.id.activity_settings2_roles_assignment_cl)
        locationsLayout = findViewById(R.id.activity_settings2_locations_cl)
        auditLogLayout = findViewById(R.id.activity_settings2_audit_log_cl)
        customFieldsLayout = findViewById(R.id.activity_settings2_customfields_cl)
        userCL = findViewById(R.id.activity_settings2_users_cl)

        mPresenter = SettingsPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        selLayout.setOnClickListener { view -> mPresenter.goToSELQuestionSets() }
        calendatLayout.setOnClickListener { view -> mPresenter.goToHolidayCalendarList() }
        rolesLayout.setOnClickListener { view -> mPresenter.goToRolesList() }
        groupsLayout.setOnClickListener { view -> mPresenter.goToGroupsList() }
        rolesAssignmentLayout.setOnClickListener { view -> mPresenter.goToRolesAssignmentList() }
        locationsLayout.setOnClickListener { view -> mPresenter.goToLocationsList() }
        auditLogLayout.setOnClickListener { view -> mPresenter.goToAuditLogSelection() }
        customFieldsLayout.setOnClickListener { view -> mPresenter.goToCustomFieldsList() }
        userCL.setOnClickListener { mPresenter.goToPeopleList() }
    }
}
