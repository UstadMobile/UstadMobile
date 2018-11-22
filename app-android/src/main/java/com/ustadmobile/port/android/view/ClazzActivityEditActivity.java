package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzActivityEditPresenter;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * This Activity is responsible for adding a new activity change for a particular class. The type of
 * feedback on the activity change and its metrics depend on the type of activity change selected.
 * The ClazzActivityEdit activity. This Activity extends UstadBaseActivity and implements
 * ClazzActivityEditView
 */
public class ClazzActivityEditActivity extends UstadBaseActivity implements ClazzActivityEditView {

    private Toolbar toolbar;
    private ClazzActivityEditPresenter mPresenter;

    //The Activity change options drop down / spinner.
    Spinner activityChangeSpinner;
    //The list of activity changes as a string list (used to populate the drop down / spinner)
    String[] changesPresets;

    //The unit of measure / length of time metric drop down / spinner that will be populated
    EditText unitOfMeasureEditText;

    //Unit of measure title
    TextView unitOfMeasureTitle;

    /**
     * Handles option selected from the toolbar. Here it is handling back button pressed.
     *
     * @param item  The menu item pressed
     * @return  true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * In Order:
     *      1. Sets layout
     *      2. Sets toolbar
     *      3. Calls the presenter and its onCreate()
     *      4. Sets the [Activity change] drop down / spinner 's on select listener -> to presenter
     *      5. Sets good / bad click listener -> to presenter
     *      6. Sets notes text edit listener -> to presenter
     *      7. Gets Unit of Measure title
     *      8. Sets the [Unit of measure] on text changed on select listener -> to presenter
     *
     * @param savedInstanceState    The application bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_clazz_activity_edit);

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_activity_edit_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        //Call the Presenter
        mPresenter = new ClazzActivityEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_clazz_activity_edit_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

        activityChangeSpinner = findViewById(R.id.activity_clazz_activity_edit_change_spinner);
        activityChangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Check and Test if +1 works
                mPresenter.handleChangeActivityChange(id+1);
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

        unitOfMeasureTitle = findViewById(R.id.activity_clazz_activity_edit_change_uom_title);

        unitOfMeasureEditText = findViewById(R.id.activity_clazz_activity_edit_change_spinner2);

        unitOfMeasureEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0)
                mPresenter.handleChangeUnitOfMeasure(Long.valueOf(s.toString()));
            }
        });
    }


    /**
     * Updates the toolbar with title given.
     *
     * @param title     The string title
     */
    @Override
    public void updateToolbarTitle(String title) {
        runOnUiThread(() -> {
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        });
    }


    /**
     * Updates the activity change drop down / spinner with values given.
     *
     * @param presets   The string array in order to be populated in the Activity change drop down /
     */
    @Override
    public void setClazzActivityChangesDropdownPresets(String[] presets) {

        this.changesPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, changesPresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityChangeSpinner.setAdapter(adapter);
    }

    @Override
    public void setUnitOfMeasureType(long uomType) {

        //Since gets called from presenter's thread, running on ui thread
        runOnUiThread(() -> {
            String uomTitle;
            switch ((int) uomType){
                case ClazzActivityChange.UOM_FREQUENCY:
                    uomTitle = getText(R.string.uom_frequency_title).toString();
                    unitOfMeasureEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                    break;
                case ClazzActivityChange.UOM_BINARY:
                    uomTitle = getText(R.string.uom_boolean_title).toString();
                    unitOfMeasureEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case ClazzActivityChange.UOM_DURATION:
                    uomTitle = getText(R.string.uom_duration_title).toString();
                    unitOfMeasureEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                default:
                    uomTitle = getText(R.string.uom_default_title).toString();
                    unitOfMeasureEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
            }
            unitOfMeasureTitle.setText(uomTitle);
        });

    }
}
