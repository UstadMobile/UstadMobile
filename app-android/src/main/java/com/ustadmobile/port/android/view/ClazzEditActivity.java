package com.ustadmobile.port.android.view;


import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzEditPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ClazzEdit activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ClazzEditView
 */
public class ClazzEditActivity extends UstadBaseActivity implements ClazzEditView {

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView scheduleRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private ClazzEditPresenter mPresenter;

    TextInputLayout classNameTIP;
    TextInputLayout classDescTIP;
    Button addScheduleButton;
    Spinner holidaySpinner;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
        setContentView(R.layout.activity_clazz_edit);

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_edit_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        scheduleRecyclerView = (RecyclerView) findViewById(
                R.id.activity_clazz_edit_schedule_recyclerview);
        mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        scheduleRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new ClazzEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Clazz Name
        classNameTIP = findViewById(R.id.activity_clazz_edit_name);
        classNameTIP.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.updateName(s.toString());
            }
        });

        //Clazz Desc
        classDescTIP = findViewById(R.id.activity_clazz_edit_description);
        classDescTIP.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.updateDesc(s.toString());
            }
        });

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_clazz_edit_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickDone());

        //Add schedule button listener
        addScheduleButton = findViewById(R.id.activity_clazz_edit_add_schedule);
        addScheduleButton.setOnClickListener(v -> mPresenter.handleClickAddSchedule());

        holidaySpinner = findViewById(R.id.activity_clazz_edit_holiday_calendar_selected);
        holidaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.updateHoliday(position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    @Override
    public void updateToolbarTitle(String titleName){
        toolbar.setTitle(titleName);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    // Diff callback.
    public static final DiffUtil.ItemCallback<Schedule> SCHEDULE_DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Schedule>() {
                @Override
                public boolean areItemsTheSame(Schedule oldItem,
                                               Schedule newItem) {
                    return oldItem.getScheduleUid() ==
                            newItem.getScheduleUid();
                }

                @Override
                public boolean areContentsTheSame(Schedule oldItem,
                                                  Schedule newItem) {
                    return oldItem.equals(newItem);
                }
            };


    @Override
    public void setClazzScheduleProvider(UmProvider<Schedule> clazzScheduleProvider) {

        ScheduleRecyclerAdapter scheduleListRecyclerAdapter =
                new ScheduleRecyclerAdapter(SCHEDULE_DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter);

        DataSource.Factory<Integer, Schedule> factory =
                (DataSource.Factory<Integer, Schedule>)
                        clazzScheduleProvider.getProvider();
        LiveData<PagedList<Schedule>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, scheduleListRecyclerAdapter::submitList);

        scheduleRecyclerView.setAdapter(scheduleListRecyclerAdapter);
    }

    @Override
    public void updateClazzEditView(Clazz updatedClazz) {

        String clazzName = "";
        String clazzDesc = "";

        if(updatedClazz != null){
            if(updatedClazz.getClazzName() != null){
                clazzName = updatedClazz.getClazzName();
            }

            if(updatedClazz.getClazzDesc() != null){
                clazzDesc = updatedClazz.getClazzDesc();
            }
        }

        String finalClazzName = clazzName;
        String finalClazzDesc = clazzDesc;
        runOnUiThread(() -> {
            Objects.requireNonNull(classNameTIP.getEditText()).setText(finalClazzName);
            Objects.requireNonNull(classDescTIP.getEditText()).setText(finalClazzDesc);
        });


    }

    @Override
    public void setHolidayPresets(String[] presets, int position) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holidaySpinner.setAdapter(adapter);
        holidaySpinner.setSelection(position);
    }

    @Override
    public void setHolidaySelected(String name, int id) {
        //nothing?
    }
}
