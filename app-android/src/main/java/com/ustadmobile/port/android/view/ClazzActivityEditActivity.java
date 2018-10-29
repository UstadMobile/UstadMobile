package com.ustadmobile.port.android.view;


import com.ustadmobile.core.controller.ClazzActivityEditPresenter;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.toughra.ustadmobile.R;


import com.ustadmobile.core.view.ClazzActivityEditView;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ClazzActivityEdit activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ClazzActivityEditView
 */
public class ClazzActivityEditActivity extends UstadBaseActivity implements ClazzActivityEditView {

    private Toolbar toolbar;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter; //replaced with object in set view provider method.
    private ClazzActivityEditPresenter mPresenter;

    Spinner activityChangeSpinner;
    String[] changesPresets;

    Spinner unitOfMeasureSpinner;
    String[] unitOfMeasurePresets;

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
        setContentView(R.layout.activity_clazz_activity_edit);

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_activity_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Call the Presenter
        mPresenter = new ClazzActivityEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_clazz_activity_edit_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton(-1));

        activityChangeSpinner = findViewById(R.id.activity_clazz_activity_edit_change_spinner);
        activityChangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleChangeActivityChange(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ImageView wentGood = findViewById(R.id.activity_clazz_activity_edit_went_good);
        wentGood.setOnClickListener(v -> mPresenter.handleChangeFeedback(true));
        ImageView wentBad = findViewById(R.id.activity_clazz_activity_edit_went_bad);
        wentBad.setOnClickListener(v -> mPresenter.handleChangeFeedback(false));

        EditText notesET = findViewById(R.id.activity_clazz_activity_edit_notes);
        notesET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleChangeNotes(s.toString());
            }
        });

        //TODO: Update Duration/Frequency/Boolean
        unitOfMeasureSpinner = findViewById(R.id.activity_clazz_activity_edit_change_spinner2);
//        unitOfMeasureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mPresenter.handleChangeUnitOfMeasure(id);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
    }


    @Override
    public void updateToolbarTitle(String title) {
        runOnUiThread(() -> {
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        });
    }



    @Override
    public void setClazzActivityChangesDropdownPresets(String[] presets) {

        this.changesPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, changesPresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityChangeSpinner.setAdapter(adapter);
    }

//    @Override
//    public void setUnitOfMeasurePresets(String[] presets) {
//        this.unitOfMeasurePresets = presets;
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
//                android.R.layout.simple_spinner_item, unitOfMeasurePresets);
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        unitOfMeasureSpinner.setAdapter(adapter);
//    }
}
