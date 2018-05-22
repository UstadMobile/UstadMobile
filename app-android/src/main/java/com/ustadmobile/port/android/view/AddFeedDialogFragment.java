package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AddFeedDialogPresenter;
import com.ustadmobile.core.view.AddFeedDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;

/**
 * Created by mike on 10/10/17.
 */

public class AddFeedDialogFragment extends UstadDialogFragment implements AddFeedDialogView,
        DialogInterface.OnClickListener, AdapterView.OnItemSelectedListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog,
        TextWatcher{

    private View rootView;

    private AlertDialog dialog;

    private AddFeedDialogPresenter mPresenter;

    private String[] dropdownPresets;

    private Spinner mPresetSpinner;

    private EditText opdsUrlEditText;

    private Button mPositiveButton;

    private TextInputLayout urlTextInputLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_add_feed_dialog, null);
        mPresetSpinner = rootView.findViewById(R.id.fragment_add_feed_dialog_spinner);
        mPresetSpinner.setOnItemSelectedListener(this);
        opdsUrlEditText = rootView.findViewById(R.id.fragment_add_feed_url_text);
        opdsUrlEditText.addTextChangedListener(this);
        urlTextInputLayout = rootView.findViewById(R.id.fragment_add_feed_url_text_input_layout);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.add_library);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.add, this);
        builder.setNegativeButton(R.string.cancel, this);
        dialog = builder.create();
        dialog.setOnShowListener(this);
        mPresenter = new AddFeedDialogPresenter(getContext(), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()), null);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        mPositiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        mPositiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    @Override
    public void onClick(View view) {
        mPresenter.handleClickAdd();
    }

    @Override
    public void setProgressVisible(boolean visible) {
        rootView.findViewById(R.id.fragment_add_feed_dialog_progress_bar).setVisibility(visible
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setDropdownPresets(String[] presets) {
        this.dropdownPresets = presets;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, presets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPresetSpinner.setAdapter(adapter);
    }

    @Override
    public void setUrlFieldVisible(boolean visible) {
        urlTextInputLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        mPresenter.handlePresetSelected(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public String getOpdsUrl() {
        return opdsUrlEditText.getText().toString();
    }

    @Override
    public void setOpdsUrl(String opdsUrl) {
        opdsUrlEditText.setText(opdsUrl);
    }

    @Override
    public void setUiEnabled(boolean enabled) {
        mPresetSpinner.setEnabled(enabled);
        opdsUrlEditText.setEnabled(enabled);
        mPositiveButton.setEnabled(enabled);
        urlTextInputLayout.setEnabled(enabled);
    }

    @Override
    public void setError(String errorMessage) {
        urlTextInputLayout.setError(errorMessage);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        mPresenter.handleOpdsUrlChanged(editable.toString());
    }
}
