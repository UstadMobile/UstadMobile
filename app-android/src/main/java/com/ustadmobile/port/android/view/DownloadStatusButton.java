package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.toughra.ustadmobile.R;

/**
 * A button that shows the download status of an item. It consists of an icon (a download icon or
 * offline pin, depending on the status), and a determinate circular progress widget to show download
 * progress.
 */
public class DownloadStatusButton extends RelativeLayout {

    private ProgressBar mProgressBar;

    private ImageView mImageView;

    public DownloadStatusButton(@NonNull Context context) {
        super(context);
        init();
    }

    public DownloadStatusButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DownloadStatusButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_download_status_button, this);
        mProgressBar = findViewById(R.id.view_download_status_button_progressbar);
        mImageView = findViewById(R.id.view_download_status_button_img);
    }

    /**
     * Setter for the progress property
     *
     * @param progress The progress of the circular progress widget that wraps around the icon (0-100)
     */
    public void setProgress(int progress){
        mProgressBar.setProgress(progress);
    }

    @ViewDebug.ExportedProperty(
        category = "progress"
    )

    /**
     * Getter for the progress property
     *
     * @return The progress of the circular progress widget that wraps around the icon (0-100)
     */
    public int getProgress() {
        return mProgressBar.getProgress();
    }

    @ViewDebug.ExportedProperty(
        category = "progress"
    )
    public int getMax() {
        return mProgressBar.getMax();
    }

    /**
     * Setter for the imageResource property
     *
     * @param resId The resource ID to use for the image to be displayed (e.g. for the download icon, offline pin, etc)
     */
    public void setImageResource(int resId) {
        mImageView.setImageResource(resId);
    }

    /**
     * Sets whether or not the progress elements of the download status are visible, so that these
     * components are only visible if a download is in progress
     *
     * @param visibility visibility flag e.g. View.GONE, View.VISIBLE, etc
     */
    public void setProgressVisibility(int visibility) {
        mProgressBar.setVisibility(visibility);
    }
}
