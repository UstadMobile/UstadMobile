package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.controller.StartDownloadPresenter;
import com.ustadmobile.port.sharedse.view.StartDownloadView;

/**
 * Android implementation of the StartDownloadView
 */
public class StartDownloadDialogFragment extends UstadDialogFragment implements StartDownloadView,
        DialogInterface.OnClickListener {

    private AlertDialog mDialog;

    private View rootView;

    private StartDownloadPresenter mPresenter;

    private ProgressBar progressBar;

    private TextView statusTextView;

    private TextView downloadSizeTextView;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_start_download_dialog, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(rootView);
        builder.setTitle(R.string.download);

        builder.setPositiveButton(R.string.download, this);
        builder.setNegativeButton(R.string.cancel, this);

        progressBar = rootView.findViewById(R.id.fragment_start_download_progress_bar);
        progressBar.setIndeterminate(true);
        statusTextView = rootView.findViewById(R.id.fragment_start_download_status_text);
        downloadSizeTextView = rootView.findViewById(R.id.fragment_start_download_size_text);
        mDialog = builder.create();
        mPresenter = new StartDownloadPresenter(getContext(), this,
                UMAndroidUtil.bundleToHashtable(getArguments()));
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        return mDialog;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch(which){
            case DialogInterface.BUTTON_POSITIVE:
                mPresenter.handleClickDownload();
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
    public void setProgressStatusText(String statusText) {
        statusTextView.setText(statusText);
    }

    @Override
    public void setDownloadText(String downloadSize) {
        downloadSizeTextView.setText(downloadSize);
    }
}
