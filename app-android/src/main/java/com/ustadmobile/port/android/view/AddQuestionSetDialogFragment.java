package com.ustadmobile.port.android.view;

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
import com.ustadmobile.core.controller.AddQuestionSetDialogPresenter;
import com.ustadmobile.core.view.AddQuestionSetDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class AddQuestionSetDialogFragment extends UstadDialogFragment implements
        AddQuestionSetDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    AddQuestionSetDialogPresenter mPresenter;
    AlertDialog dialog;
    EditText questionSetTitle;
    View rootView;


    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater!=null;
        rootView = inflater.inflate(R.layout.fragment_add_question_set_dialog, null);
        questionSetTitle =
                rootView.findViewById(R.id.fragment_add_question_set_dialog_name_edittext);


        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> mPresenter.handleAddQuestionSet(
                        questionSetTitle.getText().toString());

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> mPresenter.handleCancelSchedule();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.new_question_set);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);


        mPresenter = new AddQuestionSetDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));

        return dialog;

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    @Override
    public void onShow(DialogInterface dialogInterface) {

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void finish() {

    }
}
