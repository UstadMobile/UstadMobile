package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AddCustomFieldOptionDialogPresenter;
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class AddCustomFieldOptionDialogFragment extends UstadDialogFragment implements
        AddCustomFieldOptionDialogView,  AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    AlertDialog dialog;
    View rootView;
    AddCustomFieldOptionDialogPresenter mPresenter;

    EditText optionText;


    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        rootView = inflater.inflate(R.layout.fragment_add_custom_field_option_dialog, null);

        optionText =
                rootView.findViewById(R.id.fragment_add_question_option_dialog_text);

        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) ->
                        mPresenter.handleClickOk();

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> finish();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.add_modify_option);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.ok, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);


        mPresenter = new AddCustomFieldOptionDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));


        optionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.setOptionValue(s.toString());
            }
        });

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

    @Override
    public void setOptionValue(String optionValue) {
        runOnUiThread(() -> optionText.setText(optionValue));
    }

}
