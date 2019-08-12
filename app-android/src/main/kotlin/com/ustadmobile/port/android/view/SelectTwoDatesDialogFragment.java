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
import com.ustadmobile.core.controller.SelectTwoDatesDialogPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectTwoDatesDialogView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

/**
 * SelectTwoDatesDialogFragment Android fragment extends UstadBaseFragment
 */
public class SelectTwoDatesDialogFragment extends UstadDialogFragment
        implements SelectTwoDatesDialogView,
        AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog{

    View rootView;
    AlertDialog dialog;

    SelectTwoDatesDialogPresenter mPresenter;
    long fromDate, toDate;
    EditText fromET, toET;

    //Context (Activity calling this)
    private Context mAttachedContext;

    public interface CustomTimePeriodDialogListener{
        void onCustomTimesResult(long from, long to);
    }

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment SelectTwoDatesDialogFragment.
     */
    public static SelectTwoDatesDialogFragment newInstance() {
        SelectTwoDatesDialogFragment fragment = new SelectTwoDatesDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        Locale currentLocale = getResources().getConfiguration().locale;

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_two_dates_dialog, null);

        fromET = rootView.findViewById(R.id.fragment_select_two_dates_dialog_from_date_edittext);
        toET = rootView.findViewById(R.id.fragment_select_two_dates_dialog_to_date_edittext);

        fromET.setFocusable(false);
        toET.setFocusable(false);

        mPresenter = new SelectTwoDatesDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        Calendar myCalendar = Calendar.getInstance();

        //Date pickers's on click listener - sets text
        DatePickerDialog.OnDateSetListener toDate = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setToDate(myCalendar.getTimeInMillis());
            toET.setText(UMCalendarUtil.getPrettyDateFromLong(myCalendar.getTimeInMillis(),
                    currentLocale));
        };

        //date listener - opens a new date picker.
        DatePickerDialog dateFieldPicker = new DatePickerDialog(
                mAttachedContext, toDate, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        dateFieldPicker.getDatePicker().setMaxDate(System.currentTimeMillis());

        toET.setOnClickListener(v -> dateFieldPicker.show());

        //from
        //Date pickers's on click listener - sets text
        DatePickerDialog.OnDateSetListener fromDate = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setFromDate(myCalendar.getTimeInMillis());
            fromET.setText(UMCalendarUtil.getPrettyDateFromLong(myCalendar.getTimeInMillis(),
                    currentLocale));

        };
        //date listener - opens a new date picker.
        DatePickerDialog fromDateFieldPicker = new DatePickerDialog(
                mAttachedContext, fromDate, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        fromET.setOnClickListener(v -> fromDateFieldPicker.show());

        //Dialog's positive / negative listeners :
        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> finish();

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> System.out.println("Negative");


        //Set any view components and its listener (post presenter work)

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.custom_date_range);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();

        return dialog;

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        this.mAttachedContext = null;
        fromDate = 0L;
        toDate = 0L;
    }

    @Override
    public void finish(){
        if(mAttachedContext instanceof CustomTimePeriodDialogListener){
            ((CustomTimePeriodDialogListener) mAttachedContext).onCustomTimesResult(fromDate, toDate);
        }
        dialog.dismiss();
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
