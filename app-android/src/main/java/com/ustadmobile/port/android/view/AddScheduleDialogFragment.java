package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
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
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

import static com.ustadmobile.core.view.ClazzEditView.ARG_SCHEDULE_UID;

/**
 * The Android View for adding a schedule to Class while editing it.
 */
public class AddScheduleDialogFragment extends UstadDialogFragment implements
        AddScheduleDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    TextView errorMessageTextView;
    EditText fromET;
    EditText toET;
    Spinner scheduleSpinner;
    Spinner daySpinner;
    AddScheduleDialogPresenter mPresenter;
    AlertDialog dialog;
    View rootView;
    String[] schedulePresets;
    String[] dayPresets;

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        LayoutInflater inflater =
                (LayoutInflater)Objects.requireNonNull(getContext()).getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        Calendar myCalendar = Calendar.getInstance();
        Calendar myCalendar2 = Calendar.getInstance();

        assert inflater != null;

        if(getArguments().containsKey(ARG_SCHEDULE_UID)) {
            long currentScheduleUid = getArguments().getLong(ARG_SCHEDULE_UID);
        }

        rootView = inflater.inflate(R.layout.fragment_add_schedule_dialog, null);

        errorMessageTextView = rootView.findViewById(R.id.fragment_add_schedule_dialog_error_message);
        fromET = rootView.findViewById(R.id.fragment_add_schedule_dialog_from_time);
        toET = rootView.findViewById(R.id.fragment_add_schedule_dialog_to_time);
        scheduleSpinner = rootView.findViewById(R.id.fragment_add_schedule_dialog_schedule_spinner);
        daySpinner = rootView.findViewById(R.id.fragment_add_schedule_dialog_day_spinner);



        //Date format to show in the Date picker
        SimpleDateFormat justTheTimeFormat = new SimpleDateFormat("HH:mm");

        //A Time picker listener that sets the from time.
        TimePickerDialog.OnTimeSetListener timeF = (view, hourOfDay, minute) -> {
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minute);
            String timeOnET = justTheTimeFormat.format(myCalendar.getTime());

            mPresenter.handleScheduleFromTimeSelected(((hourOfDay * 60) + minute) * 60 * 1000);
            fromET.setText(timeOnET);
        };
        //Default view: not focusable.
        fromET.setFocusable(false);

        //From time on click -> opens a timer picker.
        fromET.setOnClickListener(v -> new TimePickerDialog(getContext(), timeF,
                myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getContext())).show());

        //A Time picker listener that sets the to time.
        TimePickerDialog.OnTimeSetListener timeT = (view, hourOfDay, minute) -> {
            myCalendar2.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar2.set(Calendar.MINUTE, minute);
            String timeOnET = justTheTimeFormat.format(myCalendar2.getTime());

            mPresenter.handleScheduleToTimeSelected(((hourOfDay * 60) + minute) * 60 * 1000);
            toET.setText(timeOnET);
        };
        //Default view: not focusable.
        toET.setFocusable(false);



        //To time on click -> opens a time picker.
        toET.setOnClickListener(v -> new TimePickerDialog(
                getContext(), timeT, myCalendar2.get(Calendar.HOUR),
                myCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getContext())).show());

        //Schedule spinner's on click listener
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

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
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

    /**
     * Adds given list of schedule presets to the Dialog Spinner
     *
     * @param presets   a string array of the presets in order.
     */
    @Override
    public void setScheduleDropdownPresets(String[] presets) {
        this.schedulePresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, schedulePresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
    }

    /**
     * Adds given list of day presets to the Dialog Spinner
     * @param presets   a string array of the presets in order.
     */
    @Override
    public void setDayDropdownPresets(String[] presets) {
        this.dayPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, dayPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);
    }

    @Override
    public void setError(String errorMessage) {    }

    @Override
    public void hideDayPicker(boolean hide) {
        if(hide){
            rootView.findViewById(R.id.fragment_add_schedule_dialog_day_heading)
                    .setVisibility(View.INVISIBLE);
            daySpinner.setVisibility(View.INVISIBLE);
        }else{
            rootView.findViewById(R.id.fragment_add_schedule_dialog_day_heading)
                    .setVisibility(View.VISIBLE);
            daySpinner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateFields(Schedule schedule) {

        runOnUiThread(() -> {
            SimpleDateFormat justTheTimeFormat = new SimpleDateFormat("HH:mm");
            String timeOnET = justTheTimeFormat.format(schedule.getSceduleStartTime());
            String timeOnToET = justTheTimeFormat.format(schedule.getScheduleEndTime());

            fromET.setText(timeOnET);
            toET.setText(timeOnToET);

            scheduleSpinner.setSelection(schedule.getScheduleFrequency() -1);
            daySpinner.setSelection(schedule.getScheduleDay() -1);
        });


    }
}
