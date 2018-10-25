package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AddScheduleDialogPresenter;
import com.ustadmobile.core.view.AddScheduleDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.annotations.NonNull;

public class AddScheduleDialogFragment extends UstadDialogFragment implements
        AddScheduleDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {


    private View rootView;
    TextView errorMessageTextView;
    EditText fromET;
    EditText toET;
    Spinner scheduleSpinner;
    Spinner daySpinner;
    AddScheduleDialogPresenter mPresenter;
    AlertDialog dialog;
    String[] schedulePresets;
    String[] dayPresets;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        Calendar myCalendar = Calendar.getInstance();
        Calendar myCalendar2 = Calendar.getInstance();

        rootView = inflater.inflate(R.layout.fragment_add_schedule_dialog, null);

        errorMessageTextView = rootView.findViewById(R.id.fragment_add_schedule_dialog_error_message);
        fromET = rootView.findViewById(R.id.fragment_add_schedule_dialog_from_time);
        toET = rootView.findViewById(R.id.fragment_add_schedule_dialog_to_time);
        scheduleSpinner = rootView.findViewById(R.id.fragment_add_schedule_dialog_schedule_spinner);
        daySpinner = rootView.findViewById(R.id.fragment_add_schedule_dialog_day_spinner);


        TimePickerDialog.OnTimeSetListener timeF = (view, hourOfDay, minute) -> {
            myCalendar.set(Calendar.HOUR, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minute);
            String timeOnET = hourOfDay + ":" + minute;
            mPresenter.handleScheduleFromTimeSelected(myCalendar.getTime().getTime());
            fromET.setText(timeOnET);
        };

        fromET.setFocusable(false);

        fromET.setOnClickListener(v -> new TimePickerDialog(getContext(), timeF,
                myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE),
                  false).show());



        TimePickerDialog.OnTimeSetListener timeT = (view, hourOfDay, minute) -> {
            myCalendar2.set(Calendar.HOUR, hourOfDay);
            myCalendar2.set(Calendar.MINUTE, minute);
            String timeOnET = hourOfDay + ":" + minute;
            mPresenter.handleScheduleToTimeSelected(myCalendar2.getTime().getTime());
            toET.setText(timeOnET);
        };

        toET.setFocusable(false);

        toET.setOnClickListener(v -> new TimePickerDialog(
                getContext(), timeT, myCalendar2.get(Calendar.HOUR),
                myCalendar.get(Calendar.MINUTE), false).show());

        scheduleSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        mPresenter.handleScheduleSelected(position, id);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleDaySelected(position,id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> mPresenter.handleAddSchedule();

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> mPresenter.handleCancelSchedule();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.add_session);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);


        mPresenter = new AddScheduleDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));

        ArrayList<String> scheduleAL = new ArrayList<>();
        scheduleAL.add(getText(R.string.daily).toString());
        scheduleAL.add(getText(R.string.weekly).toString());

        String[] s = new String[scheduleAL.size()];
        s = scheduleAL.toArray(s);

        ArrayList<String> dayAL = new ArrayList<>();
        dayAL.add(getText(R.string.sunday).toString());
        dayAL.add(getText(R.string.monday).toString());
        dayAL.add(getText(R.string.tuesday).toString());
        dayAL.add(getText(R.string.wednesday).toString());
        dayAL.add(getText(R.string.thursday).toString());
        dayAL.add(getText(R.string.friday).toString());
        dayAL.add(getText(R.string.saturday).toString());

        String[] d = new String[dayAL.size()];
        d = dayAL.toArray(d);


        setScheduleDropdownPresets(s);
        setDayDropdownPresets(d);

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        System.out.println("onClickDialog");
    }

    @Override
    public void onShow(DialogInterface dialog) {
        System.out.println("onShow");
    }

    @Override
    public void onClick(View v) {
        System.out.println("onClick");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void finish() {

    }

    @Override
    public void setScheduleDropdownPresets(String[] presets) {
        this.schedulePresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, schedulePresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);

    }

    @Override
    public void setDayDropdownPresets(String[] presets) {
        this.dayPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, dayPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);
    }

    @Override
    public void setError(String errorMessage) {

    }
}
