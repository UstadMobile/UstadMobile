package com.ustadmobile.port.android.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleListSearchPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SaleListSearchView;
import com.ustadmobile.core.view.SelectDateRangeDialogView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class SelectDateRangeDialogFragment extends UstadDialogFragment implements
        AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener,
        SelectDateRangeDialogView, DismissableDialog {

    long fromDate, toDate;
    EditText fromET, toET;

    SaleListSearchPresenter mPresenter;
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
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        Calendar myCalendarFrom = Calendar.getInstance();
        Calendar myCalendarTo = Calendar.getInstance();

        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_date_range_dialog, null);

        fromET = rootView.findViewById(R.id.fragment_select_daterange_dialog_from_time);
        toET = rootView.findViewById(R.id.fragment_select_daterange_dialog_to_time);

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

            fromET.setText(UMCalendarUtil.getPrettyDateSimpleFromLong(fromDate,
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
                (dialog, which) -> {
                    String dateRangeText = UMCalendarUtil.getPrettyDateSimpleFromLong(fromDate,
                            currentLocale) + " - " + UMCalendarUtil.getPrettyDateSimpleFromLong(toDate,
                            currentLocale);
                    mPresenter.handleDateSelected(fromDate, toDate, dateRangeText);
                };

        DialogInterface.OnClickListener negativeOCL =
                (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.date_range);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);

        mPresenter = new SaleListSearchPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), (SaleListSearchView) getActivity());
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

    //Required overrides
    @Override
    public void finish() {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onShow(DialogInterface dialog) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
