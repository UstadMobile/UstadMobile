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
import com.ustadmobile.core.controller.SaleItemDetailPresenter;
import com.ustadmobile.core.view.AddReminderDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SaleItemDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class AddReminderDialogFragment extends UstadDialogFragment implements
        AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener,
        AddReminderDialogView, DismissableDialog {

    SaleItemDetailPresenter mPresenter;
    AlertDialog dialog;
    View rootView;

    private int days;

    private Context mAttachedContext;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
    }


    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_add_reminder, null);

        NumberPicker np = rootView.findViewById(R.id.fragment_add_reminder_days_np);
        np.setMinValue(0);
        np.setMaxValue(100);

        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> {
                    days = np.getValue();
                    mPresenter.handleAddReminder(days);
                };

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> dialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.add_reminder);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);

        mPresenter = new SaleItemDetailPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()),
                (SaleItemDetailView) getActivity(), false);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));

        return dialog;
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
