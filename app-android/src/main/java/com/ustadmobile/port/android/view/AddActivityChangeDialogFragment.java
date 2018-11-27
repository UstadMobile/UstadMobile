package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AddActivityChangeDialogPresenter;
import com.ustadmobile.core.view.AddActivityChangeDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

/**
 * AddActivityChangeDialogFragment Android fragment extends UstadBaseFragment
 */
public class AddActivityChangeDialogFragment extends UstadDialogFragment implements
        AddActivityChangeDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {


    AlertDialog dialog;
    View rootView;
    String[] measurementPresets;
    Spinner measurementSpinner;
    AddActivityChangeDialogPresenter mPresenter;
    TextInputLayout titleText;

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        rootView = inflater.inflate(R.layout.fragment_add_activity_change_dialog, null);

        measurementSpinner =
                rootView.findViewById(R.id.fragment_add_activity_change_dialog_measurement_spinner);

        titleText =
                rootView.findViewById(R.id.fragment_add_activity_change_dialog_name_layout);

        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> mPresenter.handleAddActivityChange();

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> mPresenter.handleCancelActivityChange();

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.add_activity_literal);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, positiveOCL);
        builder.setNegativeButton(R.string.cancel, negativeOCL);
        dialog = builder.create();
        dialog.setOnShowListener(this);


        mPresenter = new AddActivityChangeDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));



        measurementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleMeasurementSelected(position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Objects.requireNonNull(titleText.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleTitleChanged(s.toString());
            }
        });

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

    @Override
    public void setMeasurementDropdownPresets(String[] presets) {
        this.measurementPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, measurementPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measurementSpinner.setAdapter(adapter);
    }

}
