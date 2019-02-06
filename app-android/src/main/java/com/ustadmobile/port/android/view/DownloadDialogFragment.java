package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.Objects;

public class DownloadDialogFragment extends UstadDialogFragment implements DownloadDialogView,
        DialogInterface.OnClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private View rootView;

    private AlertDialog.Builder builder;

    private AlertDialog mDialog;

    private DownloadDialogPresenter mPresenter;

    private RelativeLayout stackedOptionHolderView;

    private TextView statusTextView;

    private CheckBox wifiOnlyView;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getContext())
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_download_layout_view,null);

        stackedOptionHolderView  = rootView.findViewById(R.id.stacked_option_holder);
        statusTextView = rootView.findViewById(R.id.download_option_status_text);
        wifiOnlyView = rootView.findViewById(R.id.wifi_only_option);
        RelativeLayout wifiOnlyHolder = rootView.findViewById(R.id.wifi_only_option_holder);

        builder = new AlertDialog.Builder(getContext());
        builder.setView(rootView);

        mDialog = builder.create();
        mPresenter = new DownloadDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()),this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        wifiOnlyView.setOnCheckedChangeListener(this);
        wifiOnlyHolder.setOnClickListener(this);

        return mDialog;
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
        builder.setPositiveButton(text, this);
    }

    @Override
    public void setBottomButtonNegativeText(String text) {
        builder.setNegativeButton(text, this);
    }

    @Override
    public void setDownloadOverWifiOnly(boolean wifiOnly) {
        wifiOnlyView.setChecked(wifiOnly);
    }

    @Override
    public void setStatusText(String statusText) {
        statusTextView.setText(statusText);
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
        stackedOptionHolderView.setVisibility(visible ? View.VISIBLE:View.GONE);
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
    public void cancelDialog() {
        mDialog.dismiss();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch(which){
            case DialogInterface.BUTTON_POSITIVE:
                if(mPresenter.isDeleteFileOptions()){
                    mPresenter.handleDeleteDownloadFile();
                }else{
                    mPresenter.handleDismissDialog();
                }
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mPresenter.handleDismissDialog();
                break;
        }
    }

    @Override
    public void onClick(View stackedButton) {
        int mId = stackedButton.getId();
        if (mId == R.id.action_btn_pause_download) {
            mPresenter.handlePauseDownload();
        }else if(mId == R.id.action_btn_cancel_download){
            mPresenter.handleCancelDownload();
        }else if(mId == R.id.action_btn_continue_download){
            mPresenter.handleDismissDialog();
        }else if(mId == R.id.wifi_only_option_holder){
            mPresenter.handleWiFiOnlyOption(!wifiOnlyView.isChecked());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPresenter.handleWiFiOnlyOption(isChecked);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPresenter != null){
            mPresenter.onDestroy();
        }
    }
}
