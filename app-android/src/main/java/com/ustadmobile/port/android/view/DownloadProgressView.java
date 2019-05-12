package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;

/**
 * Created by mike on 9/22/17.
 */
public class DownloadProgressView extends LinearLayout implements View.OnClickListener{

    private ProgressBar progressBar;

    private TextView downloadPercentageTextView;

    private TextView downloadStatusTextView;

    private OnStopDownloadListener downloadStopListener;

    interface OnStopDownloadListener {
        void onClickStopDownload(DownloadProgressView view);
    }

    public DownloadProgressView(Context context) {
        super(context);
        init();
    }

    public DownloadProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DownloadProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.view_download_progress, this);
        progressBar = findViewById(R.id.view_download_progress_progressbar);
        downloadPercentageTextView = findViewById(R.id.view_download_progress_status_percentage_text);
        downloadStatusTextView = findViewById(R.id.view_download_progress_status_text);
        findViewById(R.id.view_download_progress_stop_button).setOnClickListener(this);
    }

    public float getProgress() {
        return (float)progressBar.getProgress() / 100f;
    }

    public void setProgress(float progress) {
        int progressPercentage = Math.round(progress * 100);
        progressBar.setProgress(progressPercentage);
        downloadPercentageTextView.setText(progressPercentage + "%");
    }

    public void setStatusText(String statusText) {
        downloadStatusTextView.setText(statusText);
    }

    public String getStatusText() {
        return downloadStatusTextView.getText().toString();
    }

    @Override
    public void onClick(View view) {
        if(downloadStopListener != null)
            downloadStopListener.onClickStopDownload(this);
    }

    public void setOnStopDownloadListener(OnStopDownloadListener listener) {
        this.downloadStopListener = listener;
    }
}
