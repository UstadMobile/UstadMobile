package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.Objects;

public class DownloadDialogFragment extends UstadDialogFragment implements DownloadDialogView,
        DialogInterface.OnClickListener, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {


    private View rootView;

    private AlertDialog mDialog;

    private DownloadDialogPresenter mPresenter;

    private RelativeLayout stackedOptionHolderView;

    private RelativeLayout wifiOnlyHolder;

    private TextView statusTextView;

    private CheckBox wifiOnlyView;

    private RelativeLayout calculateHolder;


    private Spinner mStorageOptions;

    private UstadMobileSystemImpl impl;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getContext())
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_download_layout_view,null);

        stackedOptionHolderView  = rootView.findViewById(R.id.stacked_option_holder);
        statusTextView = rootView.findViewById(R.id.download_option_status_text);
        wifiOnlyView = rootView.findViewById(R.id.wifi_only_option);
        mStorageOptions = rootView.findViewById(R.id.storage_option);
        calculateHolder = rootView.findViewById(R.id.download_calculate_holder);
        TextView calculateTextView = rootView.findViewById(R.id.download_dialog_calculating);
        wifiOnlyHolder = rootView.findViewById(R.id.wifi_only_option_holder);

        impl = UstadMobileSystemImpl.getInstance();

        ((TextView)rootView.findViewById(R.id.wifi_only_option_label))
                .setText(impl.getString(MessageID.download_wifi_only , getContext()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
        builder.setView(rootView);

        mDialog = builder.create();
        mPresenter = new DownloadDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()),this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        wifiOnlyView.setOnCheckedChangeListener(this);
        wifiOnlyHolder.setOnClickListener(this);
        calculateTextView.setText(impl.getString(MessageID.download_calculating,getContext()));

        setUpStorageOptions();

        return mDialog;
    }

    private void setUpStorageOptions(){

    }

    @Override
    public void setBottomButtonsVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE:View.GONE;
        Button buttonNegative =  mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button buttonPositive =  mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        buttonNegative.setVisibility(visibility);
        buttonPositive.setVisibility(visibility);
    }

    @Override
    public void setBottomButtonPositiveText(String text) {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(text);
    }

    @Override
    public void setBottomButtonNegativeText(String text) {
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(text);
    }

    @Override
    public void setDownloadOverWifiOnly(boolean wifiOnly) {
        wifiOnlyView.setChecked(wifiOnly);
    }

    @Override
    public void setStatusText(String statusText, int totalItems, String sizeInfo) {
        statusTextView.setVisibility(View.VISIBLE);
        statusTextView.setText(Html.fromHtml(String.format(statusText, totalItems, sizeInfo)));
    }



    @Override
    public void setStackedOptions(int[] optionIds, String[] optionTexts) {
        for(int i = 0; i < optionIds.length; i++){
            Button mStackedButton = rootView.findViewById(optionIds[i]);
            mStackedButton.setText(optionTexts[i]);
            mStackedButton.setOnClickListener(this);
        }
    }

    @Override
    public void setStackOptionsVisible(boolean visible) {
        stackedOptionHolderView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public int[] getOptionIds() {
        return new int[]{
                R.id.action_btn_pause_download,
                R.id.action_btn_cancel_download,
                R.id.action_btn_continue_download
        };
    }

    @Override
    public void dismissDialog() {
        mDialog.dismiss();
    }


    @Override
    public void setWifiOnlyOptionVisible(boolean visible) {
        wifiOnlyHolder.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    @Override
    public void setCalculatingViewVisible(boolean visible) {
        calculateHolder.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch(which){
            case DialogInterface.BUTTON_POSITIVE:
                mPresenter.handleClickPositive();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mPresenter.handleClickNegative();
                break;
        }
    }

    @Override
    public void onClick(View stackedButton) {
        int viewId = stackedButton.getId();
        if(viewId != R.id.wifi_only_option_holder && viewId != R.id.use_sdcard_option_holder){
            mPresenter.handleClickStackedButton(viewId);
        }else if(viewId == R.id.wifi_only_option_holder){
            mPresenter.handleWiFiOnlyOption(!wifiOnlyView.isChecked());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPresenter.handleWiFiOnlyOption(isChecked);
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        mPresenter.handleClickNegative(false);
        super.onCancel(dialog);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPresenter != null){
            mPresenter.onDestroy();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
