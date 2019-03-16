package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.RoleAssignmentDetailPresenter;
import com.ustadmobile.core.view.RoleAssignmentDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class RoleAssignmentDetailActivity extends UstadBaseActivity implements RoleAssignmentDetailView {

    private Toolbar toolbar;
    private RoleAssignmentDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private RadioGroup assignmentTypeRadioGroup;
    private Spinner groupSpinner, roleSpinner, scopeSpinner, assigneeSpinner;


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
        setContentView(R.layout.activity_role_assignment_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_role_assignment_detail_toolbar);
        toolbar.setTitle(getText(R.string.new_role_assignment));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        assignmentTypeRadioGroup = findViewById(R.id.activity_role_assignment_detail_radio_options);
        groupSpinner = findViewById(R.id.activity_role_assignment_group_spinner);
        roleSpinner = findViewById(R.id.activity_role_assignment_role_spinner);
        scopeSpinner = findViewById(R.id.activity_role_assignment_scope_spinner);
        assigneeSpinner = findViewById(R.id.activity_role_assignment_assignee_spinner);


        //Call the Presenter
        mPresenter = new RoleAssignmentDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.updateGroup(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.updateRole(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        scopeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.updateScope(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        assigneeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.updateAssignee(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_role_assignment_detail_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickDone());


    }


    @Override
    public void updateRoleAssignmentOnView(EntityRole entityRoleWithGroupName,
                                           int groupSelected, int roleSelected) {

        //Set group
        setGroupSelected(groupSelected);
        //Set role
        setRoleSelected(roleSelected);
        //Set Scope presets and scope and continue to set assignees
        setScopeAndAssigneeSelected(entityRoleWithGroupName.getErTableId());

    }

    @Override
    public void updateScopeList(int tableId){
        List<String> scopeList = new ArrayList();
        scopeList.add(getText(R.string.classes).toString());
        scopeList.add(getText(R.string.people).toString());
        scopeList.add(getText(R.string.locations).toString());

        int position=0;
        switch (tableId){
            case Clazz.TABLE_ID:
                position = 0;
                break;
            case Person.TABLE_ID:
                position=1;break;
            case Location.TABLE_ID:
                position=2;break;
        }


        String [] scopePresets = new String[scopeList.size()];
        scopePresets = scopeList.toArray(scopePresets);
        mPresenter.setScopePresets(scopePresets);

        setScopePresets(scopePresets, position);

    }

    @Override
    public void setGroupPresets(String[] presets, int position) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);
        groupSpinner.setSelection(position);
    }

    @Override
    public void setRolePresets(String[] presets, int position) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        roleSpinner.setSelection(position);
    }

    @Override
    public void setScopePresets(String[] presets, int position) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scopeSpinner.setAdapter(adapter);
        scopeSpinner.setSelection(position);
    }

    @Override
    public void setAssigneePresets(String[] presets, int position) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assigneeSpinner.setAdapter(adapter);
        assigneeSpinner.setSelection(position);
    }


    @Override
    public void setGroupSelected(int id) {
        groupSpinner.setSelection(id);
    }

    @Override
    public void setRoleSelected(int id) {
        roleSpinner.setSelection(id);
    }

    @Override
    public void setScopeSelected(int id) {
        scopeSpinner.setSelection(id);
    }

    @Override
    public void setAssigneeSelected(int id) {
        assigneeSpinner.setSelection(id);
    }

    @Override
    public void setScopeAndAssigneeSelected(int tableId) {
        updateScopeList(tableId);
        mPresenter.updateAssigneePresets(tableId);
    }
}
