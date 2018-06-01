package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;

import java.util.HashMap;
import java.util.Map;

/**
 * Android implementation of the StartDownloadView
 */
public class DownloadDialogFragment extends UstadDialogFragment implements DownloadDialogView,
        DialogInterface.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private AlertDialog mDialog;

    private View rootView;

    private DownloadDialogPresenter mPresenter;

    private ProgressBar progressBar;

    private TextView statusTextView;

    private TextView downloadSizeTextView;

    private RadioGroup radioGroup;

    private SwitchCompat wifiOnlySwitch;

    private static final Map<Integer, Integer> optionToRadioButtonIdMap = new HashMap<>();

    private static final Map<Integer, Integer> radioButtonIdToOptionIdMap = new HashMap<>();

    static {
        optionToRadioButtonIdMap.put(DownloadDialogPresenter.OPTION_START_DOWNLOAD,
                R.id.fragment_download_dialog_option_start);
        optionToRadioButtonIdMap.put(DownloadDialogPresenter.OPTION_PAUSE_DOWNLOAD,
                R.id.fragment_download_dialog_option_pause);
        optionToRadioButtonIdMap.put(DownloadDialogPresenter.OPTION_CANCEL_DOWNLOAD,
                R.id.fragment_download_dialog_option_cancel);
        optionToRadioButtonIdMap.put(DownloadDialogPresenter.OPTION_RESUME_DOWNLOAD,
                R.id.fragment_download_dialog_option_resume);
        optionToRadioButtonIdMap.put(DownloadDialogPresenter.OPTION_DELETE,
                R.id.fragment_download_dialog_option_delete);
        for(Map.Entry<Integer, Integer> entry : optionToRadioButtonIdMap.entrySet()) {
            radioButtonIdToOptionIdMap.put(entry.getValue(), entry.getKey());
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_start_download_dialog, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(rootView);
        builder.setTitle(R.string.download);

        builder.setPositiveButton(R.string.confirm, this);
        builder.setNegativeButton(R.string.cancel, this);

        progressBar = rootView.findViewById(R.id.fragment_download_dialog_progress_bar);
        progressBar.setIndeterminate(true);
        statusTextView = rootView.findViewById(R.id.fragment_download_dialog_status_text);
        downloadSizeTextView = rootView.findViewById(R.id.fragment_download_dialog_main_text);
        radioGroup = rootView.findViewById(R.id.fragment_download_dialog_options_group);
        wifiOnlySwitch = rootView.findViewById(R.id.fragment_download_dialog_download_wifi_only);
        wifiOnlySwitch.setOnCheckedChangeListener(this::onWifiOnlyCheckChanged);
        radioGroup.setOnCheckedChangeListener(this);
        mDialog = builder.create();
        mPresenter = new DownloadDialogPresenter(getContext(), this,
                UMAndroidUtil.bundleToHashtable(getArguments()));
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        return mDialog;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        mPresenter.handleSelectOption(radioButtonIdToOptionIdMap.get(checkedId));
    }

    public void onWifiOnlyCheckChanged(CompoundButton view, boolean isChecked) {
        mPresenter.handleSetWifiOnly(isChecked);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch(which){
            case DialogInterface.BUTTON_POSITIVE:
                mPresenter.handleClickConfirm();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mPresenter.handleClickCancel();
                break;
        }
    }

    @Override
    public void setProgressVisible(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        statusTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setProgress(float progress) {
        if (progress == -1){
            progressBar.setIndeterminate(true);
        }else {
            progressBar.setIndeterminate(false);
            progressBar.setProgress(Math.round(progress * 100));
        }
    }

    @Override
    public void setAvailableOptions(int options, boolean showChoices) {
        rootView.findViewById(R.id.fragment_download_dialog_options_group).setVisibility(showChoices ?
            View.VISIBLE : View.GONE);
        for(Integer choice : optionToRadioButtonIdMap.keySet()) {
            rootView.findViewById(optionToRadioButtonIdMap.get(choice))
                    .setVisibility((choice & options) == choice ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setProgressStatusText(String statusText) {
        statusTextView.setText(statusText);
    }

    @Override
    public void setMainText(String downloadSize) {
        downloadSizeTextView.setText(downloadSize);
    }
}
