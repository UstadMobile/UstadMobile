package com.ustadmobile.port.android.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportOptionsDetailPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportOptionsDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ReportOptionsDetailActivity extends UstadBaseActivity
        implements ReportOptionsDetailView {

    private Toolbar toolbar;
    private ReportOptionsDetailPresenter mPresenter;

    ConstraintLayout productTypesCL, groupByCL, showAverageCL, lesCL, locationCL, dateRangeCL;
    TextView productTypesTV, lesTV, locationTV, dateRangeTV, salesPriceTV;
    Spinner groupBySpinner;
    RangeSeekCustom rangeSeek;
    CheckBox showAverageCB;

    Menu menu;
    boolean editMode = false;

    private long fromDate, toDate;
    AlertDialog dialog;

    String[] groupByPresets;

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param thisMenu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu thisMenu) {
        menu = thisMenu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_report, menu);

        menu.findItem(R.id.menu_create_report).setVisible(true);
        if(editMode){
            menu.findItem(R.id.menu_create_report).setTitle(R.string.save);
        }else{
            menu.findItem(R.id.menu_create_report).setTitle(R.string.create_report);
        }
        return true;
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (i == R.id.menu_create_report) {
            mPresenter.handleClickCreateReport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_options_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_options_detail_toolbar);
        toolbar.setTitle(getText(R.string.report_options));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        productTypesCL = findViewById(R.id.activity_report_options_detail_product_types_cl);
        groupByCL = findViewById(R.id.activity_report_options_detail_group_by_cl);
        showAverageCL = findViewById(R.id.activity_report_options_detail_show_average_cl);
        lesCL = findViewById(R.id.activity_report_options_detail_les_cl);
        locationCL = findViewById(R.id.activity_report_options_detail_location_cl);
        dateRangeCL = findViewById(R.id.activity_report_options_detail_date_range_cl);
        rangeSeek = findViewById(R.id.activity_report_options_detail_sales_price_rangeseekcustom);
        showAverageCB = findViewById(R.id.activity_report_options_detail_show_average_cb);


        productTypesTV = findViewById(R.id.activity_report_options_detail_product_types_value);
        groupBySpinner = findViewById(R.id.activity_report_options_detail_group_by_value);
        lesTV = findViewById(R.id.activity_report_options_detail_les_value);
        locationTV = findViewById(R.id.activity_report_options_detail_location_value);
        dateRangeTV = findViewById(R.id.activity_report_options_detail_date_range_value);
        salesPriceTV = findViewById(R.id.activity_report_options_detail_sales_price_value);

        //Sales price based on range seeker
        rangeSeek.setMaxValue(100000);
        rangeSeek.setMinValue(0);


        //Call the Presenter
        mPresenter = new ReportOptionsDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Views

        rangeSeek.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            mPresenter.setFromPrice(minValue.intValue());
            mPresenter.setToPrice(maxValue.intValue());
            mPresenter.updateSalePriceRangeOnView();

        });

        //Date range
        dateRangeCL.setOnClickListener(v -> showDateRangeDialog());

        //Group by
        groupBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleChangeGroupBy(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //Product type
        productTypesCL.setOnClickListener(v -> mPresenter.goToProductSelect());

        //LEs
        lesCL.setOnClickListener(v-> mPresenter.goToLEsSelect());

        //Average:
        showAverageCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.handleToggleAverage(isChecked);
        });

        //Location
        locationCL.setOnClickListener(v -> mPresenter.goToLocationSelect());

    }

    private void showDateRangeDialog(){
        Dialog rangeDialog = createDateRangeDialog();
        rangeDialog.show();
    }


    @Override
    public void setTitle(String title) {
        runOnUiThread(() -> toolbar.setTitle(title));
    }

    @Override
    public void setShowAverage(boolean showAverage) {
        runOnUiThread(() -> showAverageCB.setChecked(showAverage));
    }

    @Override
    public void setLocationSelected(String locationSelected) {
        runOnUiThread(() -> locationTV.setText(locationSelected));
    }

    @Override
    public void setLESelected(String leSelected) {
        runOnUiThread(() -> lesTV.setText(leSelected));
    }

    @Override
    public void setProductTypeSelected(String productTypeSelected) {
        runOnUiThread(() -> productTypesTV.setText(productTypeSelected));
    }

    @Override
    public void setDateRangeSelected(String dateRangeSelected) {
        runOnUiThread(() -> dateRangeTV.setText(dateRangeSelected));
    }

    @Override
    public void setSalePriceRangeSelected(int from, int to, String salePriceSelected) {
        runOnUiThread(() -> salesPriceTV.setText(salePriceSelected));
    }

    @Override
    public void setGroupByPresets(String[] presets, int selectedPosition) {
        this.groupByPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_simple_spinner, groupByPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(() -> {
            groupBySpinner.setAdapter(adapter);
            if(selectedPosition > 0) {
                groupBySpinner.setSelection(selectedPosition);
            }
        });

    }

    @Override
    public void setEditMode(boolean editMode) {
       this.editMode = editMode;
       if(menu!=null){
           menu.getItem(R.id.menu_create_report).setTitle(editMode?R.string.save:R.string.create_report);
       }
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }


    private Dialog createDateRangeDialog(){

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE));
        Calendar myCalendarFrom = Calendar.getInstance();
        Calendar myCalendarTo = Calendar.getInstance();

        assert inflater != null;

        View rootView = inflater.inflate(R.layout.fragment_select_date_range_dialog, null);

        EditText fromET = rootView.findViewById(R.id.fragment_select_daterange_dialog_from_time);
        EditText toET = rootView.findViewById(R.id.fragment_select_daterange_dialog_to_time);

        Locale currentLocale = getResources().getConfiguration().locale;

        //TO:
        //Date pickers's on click listener - sets text
        DatePickerDialog.OnDateSetListener toDateListener = (view, year, month, dayOfMonth) -> {
            myCalendarTo.set(Calendar.YEAR, year);
            myCalendarTo.set(Calendar.MONTH, month);
            myCalendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            setToDate(myCalendarTo.getTimeInMillis());

            toET.setText(UMCalendarUtil.getPrettyDateSimpleFromLong(toDate,
                    currentLocale));
        };

        //Default view: not focusable.
        toET.setFocusable(false);

        //date listener - opens a new date picker.
        DatePickerDialog dateFieldPicker = new DatePickerDialog(
                this, toDateListener, myCalendarTo.get(Calendar.YEAR),
                myCalendarTo.get(Calendar.MONTH), myCalendarTo.get(Calendar.DAY_OF_MONTH));

        dateFieldPicker = hideYearFromDatePicker(dateFieldPicker);

        //Set onclick listener
        DatePickerDialog finalDateFieldPicker = dateFieldPicker;
        toET.setOnClickListener(v -> finalDateFieldPicker.show());

        //FROM:
        //Date pickers's on click listener - sets text
        DatePickerDialog.OnDateSetListener fromDateListener = (view, year, month, dayOfMonth) -> {
            myCalendarFrom.set(Calendar.YEAR, year);
            myCalendarFrom.set(Calendar.MONTH, month);
            myCalendarFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            setFromDate(myCalendarFrom.getTimeInMillis());

            fromET.setText(UMCalendarUtil.getPrettyDateSimpleFromLong(fromDate,
                    currentLocale));

        };

        //Default view: not focusable.
        fromET.setFocusable(false);

        //date listener - opens a new date picker.
        DatePickerDialog fromDateFieldPicker = new DatePickerDialog(
                this, fromDateListener, myCalendarFrom.get(Calendar.YEAR),
                myCalendarFrom.get(Calendar.MONTH), myCalendarFrom.get(Calendar.DAY_OF_MONTH));

        fromDateFieldPicker = hideYearFromDatePicker(fromDateFieldPicker);

        DatePickerDialog finalFromDateFieldPicker = fromDateFieldPicker;
        fromET.setOnClickListener(v -> finalFromDateFieldPicker.show());

        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> {
                    mPresenter.setFromDate(fromDate);
                    mPresenter.setToDate(toDate);
                    mPresenter.updateDateRangeOnView();

                };

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> dialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.date_range);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();

        return dialog;
    }

    public DatePickerDialog hideYearFromDatePicker(DatePickerDialog dateFieldPicker){
        try {
            Field f[] = dateFieldPicker.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mYearPicker") || field.getName().equals("mYearSpinner")
                        || field.getName().equals("mCalendarView")){
                    field.setAccessible(true);
                    Object yearPicker;
                    yearPicker = field.get(dateFieldPicker);
                    ((View) yearPicker).setVisibility(View.GONE);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return dateFieldPicker;
    }
}
