package com.ustadmobile.port.android.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AddDateRangeDialogPresenter;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.AddDateRangeDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.lib.db.entities.DateRange;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;

/**
 * The Android View for adding a DateRange to Class while editing it.
 */
public class AddDateRangeDialogFragment extends UstadDialogFragment implements
        AddDateRangeDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    long fromDate, toDate;
    EditText fromET, toET;

    AddDateRangeDialogPresenter mPresenter;
    AlertDialog dialog;
    View rootView;

    private Context mAttachedContext;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }


    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        LayoutInflater inflater =
                (LayoutInflater)Objects.requireNonNull(getContext()).getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        Calendar myCalendarFrom = Calendar.getInstance();
        Calendar myCalendarTo = Calendar.getInstance();

        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_add_daterange_dialog, null);

        fromET = rootView.findViewById(R.id.fragment_add_daterange_dialog_from_time);
        toET = rootView.findViewById(R.id.fragment_add_daterange_dialog_to_time);

        Locale currentLocale = getResources().getConfiguration().locale;

        //TO:
        //Date pickers's on click listener - sets text
        DatePickerDialog.OnDateSetListener toDateListener = (view, year, month, dayOfMonth) -> {
            myCalendarTo.set(Calendar.YEAR, year);
            myCalendarTo.set(Calendar.MONTH, month);
            myCalendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            setToDate(myCalendarTo.getTimeInMillis());
            mPresenter.handleDateRangeToTimeSelected(toDate);

            toET.setText(UMCalendarUtil.getPrettyDateSimpleWithoutYearFromLong(toDate,
                    currentLocale));
        };

        //Default view: not focusable.
        toET.setFocusable(false);

        //date listener - opens a new date picker.
        DatePickerDialog dateFieldPicker = new DatePickerDialog(
                mAttachedContext, toDateListener, myCalendarTo.get(Calendar.YEAR),
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
            mPresenter.handleDateRangeFromTimeSelected(fromDate);

            fromET.setText(UMCalendarUtil.getPrettyDateSimpleWithoutYearFromLong(fromDate,
                    currentLocale));

        };

        //Default view: not focusable.
        fromET.setFocusable(false);

        //date listener - opens a new date picker.
        DatePickerDialog fromDateFieldPicker = new DatePickerDialog(
                mAttachedContext, fromDateListener, myCalendarFrom.get(Calendar.YEAR),
                myCalendarFrom.get(Calendar.MONTH), myCalendarFrom.get(Calendar.DAY_OF_MONTH));

        fromDateFieldPicker = hideYearFromDatePicker(fromDateFieldPicker);

        DatePickerDialog finalFromDateFieldPicker = fromDateFieldPicker;
        fromET.setOnClickListener(v -> finalFromDateFieldPicker.show());

        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> mPresenter.handleAddDateRange();

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> mPresenter.handleCancelDateRange();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.add_calendar_range);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);

        mPresenter = new AddDateRangeDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));

        return dialog;
    }

    public DatePickerDialog hideYearFromDatePicker(DatePickerDialog dateFieldPicker){
        try {
            Field f[] = dateFieldPicker.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mYearPicker") || field.getName().equals("mYearSpinner")
                        || field.getName().equals("mCalendarView")){
                    field.setAccessible(true);
                    Object yearPicker = new Object();
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

    @Override
    public void onClick(DialogInterface dialog, int which) {  }

    @Override
    public void onShow(DialogInterface dialog) {  }

    @Override
    public void onClick(View v) {  }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {   }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {    }

    @Override
    public void finish() { }


    @Override
    public void setError(String errorMessage) {    }

    @Override
    public void updateFields(DateRange daterange) {

        runOnUiThread(() -> {


            long startTimeLong = daterange.getDateRangeFromDate();
            long endTimeLong = daterange.getDateRangeToDate();


            fromET.setText(UMCalendarUtil.getPrettySuperSimpleDateSimpleWithoutYearFromLong(
                    startTimeLong));
            toET.setText(UMCalendarUtil.getPrettySuperSimpleDateSimpleWithoutYearFromLong(
                    endTimeLong));

        });


    }
}
