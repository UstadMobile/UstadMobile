package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.fs.presenter.StartDownloadPresenter;
import com.ustadmobile.core.fs.view.StartDownloadView;

/**
 * Created by mike on 3/5/18.
 */

public class StartDownloadDialogFragment extends UstadDialogFragment implements StartDownloadView,
        DialogInterface.OnClickListener{

    private AlertDialog mDialog;

    private View rootView;

    private StartDownloadPresenter mPresenter;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_start_download_dialog, null);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(rootView);
        builder.setTitle(R.string.download);

        builder.setPositiveButton(R.string.download, this);
        builder.setNegativeButton(R.string.cancel, this);

        mDialog = builder.create();
        mPresenter = new StartDownloadPresenter(getContext(), this, null);

        return mDialog;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {

    }

    @Override
    public void setProgressVisible(boolean visible) {

    }

    @Override
    public void setProgress(float progress) {

    }

    @Override
    public void setProgressStatusText(String statusText) {

    }

    @Override
    public void setDownloadSize(String downloadSize) {

    }
}
