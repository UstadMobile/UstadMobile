package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBar

import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.RoleAssignmentDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.RoleAssignmentDetailView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.EntityRole
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.UstadBaseActivity

import java.util.ArrayList
import java.util.Objects

import ru.dimorinny.floatingtextbutton.FloatingTextButton

class RoleAssignmentDetailActivity : UstadBaseActivity(), RoleAssignmentDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: RoleAssignmentDetailPresenter? = null
    private val mRecyclerView: RecyclerView? = null
    private var assignmentTypeRadioGroup: RadioGroup? = null
    private var individualRB: RadioButton? = null
    private var groupRB: RadioButton? = null
    private var groupName: TextView? = null
    private var groupSpinner: Spinner? = null
    private var roleSpinner: Spinner? = null
    private var scopeSpinner: Spinner? = null
    private var assigneeSpinner: Spinner? = null


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

    override fun individualClicked() {
        runOnUiThread {
            individualRB!!.isChecked = true
            groupRB!!.isChecked = false
        }
    }

    override fun groupClicked() {
        runOnUiThread {
            individualRB!!.isChecked = true
            groupRB!!.isChecked = false
        }

    }

    override fun updateGroupName(individual: Boolean) {
        runOnUiThread {
            if (individual) {
                groupName!!.setText(R.string.person)
            } else {
                groupName!!.setText(R.string.role_group)
            }
        }

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_role_assignment_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_role_assignment_detail_toolbar)
        toolbar!!.title = getText(R.string.new_role_assignment)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        assignmentTypeRadioGroup = findViewById(R.id.activity_role_assignment_detail_radio_options)
        groupName = findViewById(R.id.activity_role_assignment_group)
        groupSpinner = findViewById(R.id.activity_role_assignment_group_spinner)
        roleSpinner = findViewById(R.id.activity_role_assignment_role_spinner)
        scopeSpinner = findViewById(R.id.activity_role_assignment_scope_spinner)
        assigneeSpinner = findViewById(R.id.activity_role_assignment_assignee_spinner)
        individualRB = findViewById(R.id.activity_role_assignment_detail_user_option)
        groupRB = findViewById(R.id.activity_role_assignment_detail_group_option)

        //Update heading:
        assignmentTypeRadioGroup!!.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.activity_role_assignment_detail_user_option) {
                updateGroupName(true)
                mPresenter!!.updateGroupList(true)
            } else if (checkedId == R.id.activity_role_assignment_detail_group_option) {
                updateGroupName(false)
                mPresenter!!.updateGroupList(false)
            }
        }


        //Call the Presenter
        mPresenter = RoleAssignmentDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        groupSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.updateGroup(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        roleSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.updateRole(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        scopeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.updateScope(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        assigneeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.updateAssignee(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_role_assignment_detail_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }


    }


    override fun updateRoleAssignmentOnView(entityRoleWithGroupName: EntityRole,
                                            groupSelected: Int, roleSelected: Int) {

        runOnUiThread(Runnable {
            //Set group
            setGroupSelected(groupSelected)
            //Set role
            setRoleSelected(roleSelected)
            //Set Scope presets and scope and continue to set assignees
            setScopeAndAssigneeSelected(entityRoleWithGroupName.erTableId)
        })
    }

    override fun updateScopeList(tableId: Int) {
        val scopeList = ArrayList<String>()
        scopeList.add(getText(R.string.classes).toString())
        scopeList.add(getText(R.string.people).toString())
        scopeList.add(getText(R.string.locations).toString())

        var position = 0
        when (tableId) {
            Clazz.TABLE_ID -> position = 0
            Person.TABLE_ID -> position = 1
            Location.TABLE_ID -> position = 2
        }


        var scopePresets = arrayOfNulls<String>(scopeList.size)
        scopePresets = scopeList.toTypedArray()
        mPresenter!!.setScopePresets(scopePresets)

        setScopePresets(scopePresets, position)

    }

    override fun setGroupPresets(presets: Array<String>, position: Int) {
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, presets)
        groupSpinner!!.adapter = adapter
        groupSpinner!!.setSelection(position)
    }

    override fun setRolePresets(presets: Array<String>, position: Int) {
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, presets)
        roleSpinner!!.adapter = adapter
        roleSpinner!!.setSelection(position)
    }

    override fun setScopePresets(presets: Array<String?>, position: Int) {
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, presets)
        scopeSpinner!!.adapter = adapter
        scopeSpinner!!.setSelection(position)
    }

    override fun setAssigneePresets(presets: Array<String>, position: Int) {
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, presets)
        assigneeSpinner!!.adapter = adapter
        assigneeSpinner!!.setSelection(position)
    }


    override fun setGroupSelected(id: Int) {
        groupSpinner!!.setSelection(id)
    }

    override fun setRoleSelected(id: Int) {
        roleSpinner!!.setSelection(id)
    }

    override fun setScopeSelected(id: Int) {
        scopeSpinner!!.setSelection(id)
    }

    override fun setAssigneeSelected(id: Int) {
        assigneeSpinner!!.setSelection(id)
    }

    override fun setScopeAndAssigneeSelected(tableId: Int) {
        updateScopeList(tableId)
        mPresenter!!.updateAssigneePresets(tableId)
    }
}
