package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.RecordDropoutDialogPresenter;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.RecordDropoutDialogView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class RecordDropoutDialogFragment extends UstadDialogFragment  implements
        RecordDropoutDialogView,  AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    RecordDropoutDialogPresenter mPresenter;
    AlertDialog dialog;
    View rootView;

    CheckBox otherNGO, move, cry, sick, permission, school, transportation, personal, other;

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_record_dropout_dialog, null);


        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> mPresenter.handleClickOk();

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> finish();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.record_dropout);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.ok, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);



        mPresenter = new RecordDropoutDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));

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
}
