package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.NumberPicker;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportAttendanceGroupedByThresholdsPresenter;
import com.ustadmobile.core.controller.SelectAttendanceThresholdsDialogPresenter;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectAttendanceThresholdsDialogView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import io.reactivex.annotations.NonNull;

import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_HIGH;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_LOW;
import static com.ustadmobile.core.view.ReportEditView.ARG_THRESHOLD_MID;
import static com.ustadmobile.core.view.ReportEditView.THRESHOLD_HIGH_DEFAULT;
import static com.ustadmobile.core.view.ReportEditView.THRESHOLD_LOW_DEFAULT;
import static com.ustadmobile.core.view.ReportEditView.THRESHOLD_MED_DEFAULT;

/**
 * SelectAttendanceThresholdsDialogFragment Android fragment extends UstadBaseFragment
 */
public class SelectAttendanceThresholdsDialogFragment extends UstadDialogFragment
        implements SelectAttendanceThresholdsDialogView,
        AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    View rootView;
    SelectAttendanceThresholdsDialogPresenter mPresenter;
    AlertDialog dialog;

    NumberPicker lowNumberPicker, midNumberPicker, highNumberPicker;

    //Context (Activity calling this)
    private Context mAttachedContext;

    private ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues selectedValues;

    //Activity should implement this.
    public interface ThresholdsSelectedDialogListener{
        void onThresholdResult(ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues values);
    }

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment SelectAttendanceThresholdsDialogFragment.
     */
    public static SelectAttendanceThresholdsDialogFragment newInstance() {
        SelectAttendanceThresholdsDialogFragment fragment = new SelectAttendanceThresholdsDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setUpNP(NumberPicker np, Integer setVal){

        np.setMinValue(1);
        np.setMaxValue(100);

        np.setWrapSelectorWheel(false);

        String[] nums = new String[101];
        for(int i=1; i<nums.length; i++)
            nums[i] = Integer.toString(i);

        np.setValue(setVal);
    }

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_attendance_thresholds_dialog, null);

        lowNumberPicker = rootView.findViewById(R.id.fragment_select_attendance_thresholds_dialog_number_picker_low);
        midNumberPicker = rootView.findViewById(R.id.fragment_select_attendance_thresholds_dialog_number_picker_medium);
        highNumberPicker = rootView.findViewById(R.id.fragment_select_attendance_thresholds_dialog_number_picker_high);

        assert getArguments() != null;
        if(getArguments().containsKey(ARG_THRESHOLD_LOW)){
            setUpNP(lowNumberPicker, (Integer) getArguments().get(ARG_THRESHOLD_LOW));
        }else{
            setUpNP(lowNumberPicker, THRESHOLD_LOW_DEFAULT);
        }
        if(getArguments().containsKey(ARG_THRESHOLD_MID)){
            setUpNP(midNumberPicker, (Integer) getArguments().get(ARG_THRESHOLD_MID));
        }else{
            setUpNP(midNumberPicker, THRESHOLD_MED_DEFAULT);
        }
        if(getArguments().containsKey(ARG_THRESHOLD_HIGH)){
            setUpNP(highNumberPicker, (Integer) getArguments().get(ARG_THRESHOLD_HIGH));
        }else{
            setUpNP(highNumberPicker, THRESHOLD_HIGH_DEFAULT);
        }



        mPresenter = new SelectAttendanceThresholdsDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Dialog stuff:
        //Dialog's positive / negative listeners :
        DialogInterface.OnClickListener positiveOCL = (dialog, which) -> {

            selectedValues.low = lowNumberPicker.getValue();
            selectedValues.med = midNumberPicker.getValue();
            selectedValues.high = highNumberPicker.getValue();

            finish();
        };


        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> selectedValues = null;

        //Set any view components and its listener (post presenter work)


        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.select_attendance_thresholds);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.done, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();

        return dialog;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
        selectedValues = new ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        this.mAttachedContext = null;
        selectedValues = null;
    }

    @Override
    public void finish(){
        if(mAttachedContext instanceof ThresholdsSelectedDialogListener){
            ((ThresholdsSelectedDialogListener) mAttachedContext).onThresholdResult(selectedValues);
        }
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onShow(DialogInterface dialog) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
