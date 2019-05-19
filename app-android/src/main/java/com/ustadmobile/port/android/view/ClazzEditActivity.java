package com.ustadmobile.port.android.view;


import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzEditPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ClazzEdit activity - responsible for the Class Edit screen activity on Android.
 * The ClazzEdit screen has Schedule recycler view and a DateRange calendar spinner along side
 * EditText for Class name and description.
 *
 * This Activity extends UstadBaseActivity and implements ClazzEditView
 */
public class ClazzEditActivity extends UstadBaseActivity implements ClazzEditView,
        SelectClazzFeaturesDialogFragment.ClazzFeaturesSelectDialogListener {

    private Toolbar toolbar;

    private RecyclerView scheduleRecyclerView;
    private ClazzEditPresenter mPresenter;

    TextInputLayout classNameTIP;
    TextInputLayout classDescTIP;
    Button addScheduleButton;
    Spinner holidaySpinner, locationSpinner;

    TextView featuresTextView;

    LinearLayout customFieldsLL;

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
        toolbar.setTitle(R.string.class_setup);

        //Recycler View:
        scheduleRecyclerView = findViewById(
                R.id.activity_clazz_edit_schedule_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        scheduleRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        featuresTextView = findViewById(R.id.activity_clazz_edit_features_selected);

        customFieldsLL = findViewById(R.id.activity_clazz_edit_customfields_ll);

        //Call the Presenter
        mPresenter = new ClazzEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        featuresTextView.setOnClickListener(v -> mPresenter.handleClickFeaturesSelection());

        //Clazz Name
        classNameTIP = findViewById(R.id.activity_clazz_edit_name);
        Objects.requireNonNull(classNameTIP.getEditText()).addTextChangedListener(new TextWatcher() {
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
        Objects.requireNonNull(classDescTIP.getEditText()).addTextChangedListener(new TextWatcher() {
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
        fab.setOnClickListener(v -> handleClickDone());

        //Add schedule button listener
        addScheduleButton = findViewById(R.id.activity_clazz_edit_add_schedule);
        addScheduleButton.setOnClickListener(v -> mPresenter.handleClickAddSchedule());

        //DateRange Spinner (drop-down)
        holidaySpinner = findViewById(R.id.activity_clazz_edit_holiday_calendar_selected);
        holidaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setHolidaySelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        locationSpinner = findViewById(R.id.activity_clazz_edit_location_spinner);
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setLocationSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    private void handleClickDone(){

        int customCount = customFieldsLL.getChildCount();
        for(int i=0; i< customCount; i++){
            View field = customFieldsLL.getChildAt(i);
            int fieldId = field.getId();
            int type = 0;
            Object valueObject = null;
            if(field instanceof TextInputLayout){
                //Text custom field
                type = CustomField.FIELD_TYPE_TEXT;
                TextInputLayout til = (TextInputLayout)field;
                valueObject = til.getEditText().getText().toString();

            }else if(field instanceof LinearLayout){
                LinearLayout vll = (LinearLayout) field;
                type = CustomField.FIELD_TYPE_DROPDOWN;
                Spinner s = (Spinner) vll.getChildAt(1);
                valueObject = s.getSelectedItemPosition();
            }
            mPresenter.handleSaveCustomFieldValues(fieldId,type, valueObject);
        }

        mPresenter.handleClickDone();
    }

    @Override
    public void updateToolbarTitle(String titleName){
        toolbar.setTitle(titleName);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.class_setup);
    }

    // Diff callback.
    public static final DiffUtil.ItemCallback<Schedule> SCHEDULE_DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Schedule>() {
                @Override
                public boolean areItemsTheSame(Schedule oldItem, Schedule newItem) {
                    return oldItem.getScheduleUid() == newItem.getScheduleUid();
                }

                @Override
                public boolean areContentsTheSame(Schedule oldItem, Schedule newItem) {
                    return oldItem.equals(newItem);
                }
            };


    @Override
    public void setClazzScheduleProvider(UmProvider<Schedule> clazzScheduleProvider) {

        ScheduleRecyclerAdapter scheduleListRecyclerAdapter =
                new ScheduleRecyclerAdapter(SCHEDULE_DIFF_CALLBACK, getApplicationContext(),
                        this, mPresenter);

        //Unchecked warning is expected.
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

        String featuresText="";
        if(updatedClazz.isAttendanceFeature()){
            String addComma="";
            if(!featuresText.equals("")){
                addComma =",";
            }
            featuresText =  featuresText + addComma + " " + getText(R.string.attendance) ;
        }
        if(updatedClazz.isActivityFeature()){
            String addComma="";
            if(!featuresText.equals("")){
                addComma =",";
            }
            featuresText = featuresText + addComma + " " +getText(R.string.activity_change) ;
        }
        if(updatedClazz.isSelFeature()){
            String addComma="";
            if(!featuresText.equals("")){
                addComma =",";
            }
            featuresText = featuresText + addComma + " " +getText(R.string.sel_caps) ;
        }
        featuresTextView.setText(featuresText);

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
                R.layout.item_simple_spinner, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holidaySpinner.setAdapter(adapter);
        holidaySpinner.setSelection(position);
    }

    @Override
    public void setLocationPresets(String[] presets, int position) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setSelection(position);
    }

    /**
     * Handles holiday selected
     * @param position    The id/position of the DateRange selected from the spinner.
     */
    @Override
    public void setHolidaySelected(int position) {
        mPresenter.updateHoliday(position);
    }

    @Override
    public void setLocationSelected(int position){
        mPresenter.updateLocation(position);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void addCustomFieldText(CustomField label, String value) {
        //customFieldsLL

        //Calculate the width of the screen.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;

        //The field is an input type. So we are gonna add a TextInputLayout:
        TextInputLayout fieldTextInputLayout = new TextInputLayout(this);
        int viewId = View.generateViewId();
        mPresenter.addToMap(viewId, label.getCustomFieldUid());
        fieldTextInputLayout.setId(viewId);
        //Edit Text is inside a TextInputLayout
        TextInputLayout.LayoutParams textInputLayoutParams =
                new TextInputLayout.LayoutParams(displayWidth,
                        TextInputLayout.LayoutParams.MATCH_PARENT);

        int widthWithPadding = displayWidth - dpToPx(28);
        //The EditText
        EditText fieldEditText = new EditText(this);
        fieldEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ViewGroup.LayoutParams editTextParams =
                new LinearLayout.LayoutParams(
                        widthWithPadding,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        fieldEditText.setLayoutParams(editTextParams);
        fieldEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        fieldEditText.setText(value);

        fieldEditText.setHint(label.getCustomFieldName());

        fieldTextInputLayout.addView(fieldEditText, textInputLayoutParams);
        fieldTextInputLayout.setPadding(dpToPx(8),0,0,0);
        customFieldsLL.addView(fieldTextInputLayout);
    }

    @Override
    public void addCustomFieldDropdown(CustomField label, String[] options, int selected) {
        //Calculate the width of the screen.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;

        //The field is an input type. So we are gonna add a TextInputLayout:
        //Edit Text is inside a TextInputLayout
        TextInputLayout.LayoutParams textInputLayoutParams =
                new TextInputLayout.LayoutParams(displayWidth,
                        TextInputLayout.LayoutParams.MATCH_PARENT);

        int widthWithPadding = displayWidth - dpToPx(28);

        //Spinner time
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                        options);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setSelection(selected);

        //Spinner label
        TextView labelTV = new TextView(this);
        labelTV.setText(label.getCustomFieldName());

        int viewId = View.generateViewId();
        mPresenter.addToMap(viewId, label.getCustomFieldUid());

        //VLL
        LinearLayout vll = new LinearLayout(this);
        vll.setId(viewId);
        vll.setLayoutParams(textInputLayoutParams)  ;
        vll.setOrientation(LinearLayout.VERTICAL);

        vll.addView(labelTV);
        vll.addView(spinner);

        vll.setPadding(dpToPx(8),0,0,0);
        customFieldsLL.addView(vll);
    }

    @Override
    public void clearAllCustomFields() {
        customFieldsLL.removeAllViews();
    }

    @Override
    public void onSelectClazzesFeaturesResult(Clazz clazz) {
        featuresTextView.setText("");
        String featuresText="";

        if(clazz.isAttendanceFeature()){
            String addComma="";
            if(!featuresText.equals("")){
                addComma =",";
            }
            featuresText = featuresText + addComma + " " + getText(R.string.attendance);
        }
        if(clazz.isActivityFeature()){
            String addComma="";
            if(!featuresText.equals("")){
                addComma =",";
            }
            featuresText = featuresText + addComma + " " + getText(R.string.activity_change);
        }
        if(clazz.isSelFeature()){
            String addComma="";
            if(!featuresText.equals("")){
                addComma =",";
            }
            featuresText = featuresText + addComma + " " + getText(R.string.sel_caps);
        }
        featuresTextView.setText(featuresText);
        mPresenter.updateFeatures(clazz);
    }
}
