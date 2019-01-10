package com.ustadmobile.port.android.view;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzActivityEditPresenter;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.controller.ClazzActivityEditPresenter.FALSE_ID;
import static com.ustadmobile.core.controller.ClazzActivityEditPresenter.TRUE_ID;


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
    AppCompatSpinner activityChangeSpinner;
    Spinner trueFalseSpinner;

    //The list of activity changes as a string list (used to populate the drop down / spinner)
    String[] changesPresets;

    //The unit of measure / length of time metric drop down / spinner that will be populated
    EditText unitOfMeasureEditText;

    //Good and Bad thumbs up
    ImageView thumbsUp, thumbsDown;

    //Unit of measure title
    TextView unitOfMeasureTitle;

    //Notes in the ClazzActivity
    TextView notesET;

    //Date heading
    TextView dateHeading;

    //Back and Forward date
    ImageView backDate, forwardDate;

    //FAB
    FloatingTextButton fab;

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
     *      3. Calls the presenter and its onCreate() < sets the spinner, fills data
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

        //Get items
        thumbsUp = findViewById(R.id.activity_clazz_activity_edit_went_good);
        thumbsDown = findViewById(R.id.activity_clazz_activity_edit_went_bad);
        notesET = findViewById(R.id.activity_clazz_activity_edit_notes);
        unitOfMeasureTitle = findViewById(R.id.activity_clazz_activity_edit_change_uom_title);
        unitOfMeasureEditText = findViewById(R.id.activity_clazz_activity_edit_change_spinner2);
        dateHeading = findViewById(R.id.activity_class_activity_date_heading3);
        backDate = findViewById(R.id.activity_class_activity_date_go_back3);
        forwardDate = findViewById(R.id.activity_class_activity_date_go_forward3);
        backDate = findViewById(R.id.activity_class_activity_date_go_back3);
        forwardDate = findViewById(R.id.activity_class_activity_date_go_forward3);
        fab = findViewById(R.id.activity_clazz_activity_edit_fab);
        trueFalseSpinner = findViewById(R.id.activity_clazz_activity_edit_change_measurement_spinner);
        activityChangeSpinner = findViewById(R.id.activity_clazz_activity_edit_change_spinner);

        //Call the Presenter
        mPresenter = new ClazzActivityEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Change icon based on rtl in current language (eg: arabic)
        int isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault());
        switch (isLeftToRight){
            case ViewCompat.LAYOUT_DIRECTION_RTL:
                backDate.setImageDrawable(getDrawable(R.drawable.ic_chevron_right_black_24dp));
                forwardDate.setImageDrawable(getDrawable(R.drawable.ic_chevron_left_black_24dp));
        }

        //FAB and its listener
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());

        activityChangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //The change to id map starts from an offset
                mPresenter.handleChangeActivityChange(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setTrueFalseDropdownPresets();
        trueFalseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Handle true/false value selected from the spinner.
                mPresenter.handleChangeTrueFalseMeasurement(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Thumbs listener
        thumbsUp.setOnClickListener(v -> mPresenter.handleChangeFeedback(true));
        thumbsDown.setOnClickListener(v -> mPresenter.handleChangeFeedback(false));

        //Notes listener
        notesET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleChangeNotes(s.toString());
            }
        });


        //Unit of Measure text listener
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

        //Date switching
        backDate.setOnClickListener(v -> mPresenter.handleClickGoBackDate());
        forwardDate.setOnClickListener(v -> mPresenter.handleClickGoForwardDate());
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

    public void setTrueFalseDropdownPresets(){

        ArrayList<String> presetAL = new ArrayList<>();
        presetAL.add("True");
        presetAL.add("False");

        String[] trueFalsePresets = presetAL.toArray(new String[presetAL.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, trueFalsePresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trueFalseSpinner.setAdapter(adapter);
    }

    /**
     * Updates the activity change drop down / spinner with values given.
     *
     * @param presets   The string array in order to be populated in the Activity change drop down /
     */
    @Override
    public void setClazzActivityChangesDropdownPresets(String[] presets) {

        this.changesPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
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
                    setTrueFalseVisibility(false);
                    break;
                case ClazzActivityChange.UOM_BINARY:
                    uomTitle = getText(R.string.uom_boolean_title).toString();
                    setTrueFalseVisibility(true);
                    break;
                case ClazzActivityChange.UOM_DURATION:
                    uomTitle = getText(R.string.uom_duration_title).toString();
                    unitOfMeasureEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    setTrueFalseVisibility(false);
                    break;
                default:
                    uomTitle = getText(R.string.uom_default_title).toString();
                    unitOfMeasureEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    setTrueFalseVisibility(false);
                    break;
            }
            unitOfMeasureTitle.setText(uomTitle);

        });

    }

    @Override
    public void setActivityChangeOption(long option) {
        activityChangeSpinner.setSelection((int) option);
    }

    @Override
    public void setThumbs(int thumbs) {
        runOnUiThread(() -> {
            switch (thumbs){
                case THUMB_OFF:
                    thumbsUp.clearColorFilter();
                    thumbsDown.clearColorFilter();
                    break;
                case THUMB_GOOD:
                    thumbsUp.setColorFilter(Color.BLACK);
                    thumbsDown.clearColorFilter();
                    break;
                case THUMB_BAD:
                    thumbsUp.clearColorFilter();
                    thumbsDown.setColorFilter(Color.BLACK);
                    break;
            }
        });
    }

    @Override
    public void setNotes(String notes) {
        runOnUiThread(() -> notesET.setText(notes));
    }

    @Override
    public void setUOMText(String uomText) {
        runOnUiThread(() ->unitOfMeasureEditText.setText(uomText));
    }

    @Override
    public void setMeasureBitVisibility(boolean visible) {
        runOnUiThread(() -> {
            if (visible) {
                unitOfMeasureTitle.setVisibility(View.VISIBLE);
                unitOfMeasureEditText.setVisibility(View.VISIBLE);
            } else {
                unitOfMeasureTitle.setVisibility(View.INVISIBLE);
                unitOfMeasureEditText.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void setTrueFalseVisibility(boolean visible) {
        runOnUiThread(() -> {
            if (visible) {
                trueFalseSpinner.setVisibility(View.VISIBLE);
                unitOfMeasureEditText.setVisibility(View.INVISIBLE);
            } else {
                trueFalseSpinner.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Sets the dateString to the View
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    @Override
    public void updateDateHeading(String dateString) {
        //Since its called from the presenter, need to run on ui thread.
        runOnUiThread(() -> dateHeading.setText(dateString));
    }

    @Override
    public void showFAB(boolean show) {
        if(show){
            fab.setVisibility(View.VISIBLE);
        }else{
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setEditable(boolean editable) {

        activityChangeSpinner.setEnabled(editable);
        notesET.setEnabled(editable);
        unitOfMeasureEditText.setEnabled(editable);
        thumbsUp.setEnabled(editable);
        thumbsDown.setEnabled(editable);
        unitOfMeasureEditText.setEnabled(editable);
        trueFalseSpinner.setEnabled(editable);

        showFAB(editable);

    }
}
