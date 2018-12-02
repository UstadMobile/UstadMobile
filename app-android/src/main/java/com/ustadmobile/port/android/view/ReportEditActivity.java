package com.ustadmobile.port.android.view;


import com.ustadmobile.core.controller.ReportEditPresenter;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.toughra.ustadmobile.R;


import com.ustadmobile.core.view.ReportEditView;

import java.util.HashMap;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ReportEdit activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ReportEditView
 */
public class ReportEditActivity extends UstadBaseActivity implements ReportEditView {

    private Toolbar toolbar;

    private TextView locationsTextView;
    private Spinner timePeriodSpinner;
    private TextView heading;
    private CheckBox genderDisaggregateCheck;
    private ReportEditPresenter mPresenter;
    private Spinner classesSpinner;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_edit);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        locationsTextView = findViewById(R.id.activity_report_edit_location_detail);
        timePeriodSpinner = findViewById(R.id.activity_report_edittime_period_spinner);
        heading = findViewById(R.id.activity_report_edit_report_title);
        genderDisaggregateCheck = findViewById(R.id.activity_report_edit_gender);
        classesSpinner = findViewById(R.id.activity_report_classes_spinner);

        //Call the Presenter
        mPresenter = new ReportEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleTimePeriodSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //TODO: Maybe we don't need this but need a custom multi spinner selector
        classesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleClassesSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        genderDisaggregateCheck.setOnCheckedChangeListener(
                (buttonView, isChecked) -> mPresenter.setGenderDisaggregate(isChecked));


        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_report_edit_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }


    @Override
    public void populateTimePeriod(HashMap<Integer, String> options) {

        String[] timePeriodPresets = options.values().toArray(new String[options.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, timePeriodPresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePeriodSpinner.setAdapter(adapter);
    }

    @Override
    public void updateClassesSet(HashMap<Long, String> options) {

        String[] classesPresets = options.values().toArray(new String[options.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, classesPresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classesSpinner.setAdapter(adapter);

    }

    @Override
    public void updateLocationsSelected(String locations) {
        locationsTextView.setText(locations);
    }

    @Override
    public void updateGenderDisaggregationSet(boolean byGender) {
        genderDisaggregateCheck.setChecked(byGender);
    }

    @Override
    public void updateReportName(String name) {
        heading.setText(name);
    }
}
