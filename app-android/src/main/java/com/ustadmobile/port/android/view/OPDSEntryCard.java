/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;
import com.ustadmobile.port.android.util.UmAndroidImageUtil;

import java.util.HashMap;

/**
 * Created by mike on 08/08/15.
 *
 * TODO: There's a lot to do here... it's a work in progress as this is shifts to using LiveData and OpdsEntryStatusCache
 */
public class OPDSEntryCard extends ConstraintLayout {

    private OpdsEntryWithStatusCache opdsEntry;

    private DownloadStatusButton statusButton;

    private static final HashMap<Integer, Integer> STATUS_TO_COLOR_MAP = new HashMap<>();

    private String currentThumbnailUrl;

    private static final int IN_PROGRESS_THRESHOLD = 90;

    static {
        STATUS_TO_COLOR_MAP.put(MessageID.in_progress, R.color.entry_learner_progress_in_progress);
        STATUS_TO_COLOR_MAP.put(MessageID.failed_message, R.color.entry_learner_progresss_failed);
        STATUS_TO_COLOR_MAP.put(MessageID.passed, R.color.entry_learner_progress_passed);
    }

    /**
     * The 100% amount of the progress bar; defined as 100
     */
    public static final int PROGRESS_ENTRY_MAX = 100;

    private DownloadProgressView mDownloadProgressView;


    public interface OnClickDownloadListener{

        void onClickDownload(OpdsEntryWithRelations entry);

    }

    private OnClickDownloadListener onClickDownloadListener;

    public OPDSEntryCard(Context ctx) {
        super(ctx);
        init();
    }

    public OPDSEntryCard(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    public OPDSEntryCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    private void init() {
        inflate(getContext(), R.layout.item_opds_entry_card, this);
        statusButton = findViewById(R.id.item_opds_entry_card_download_icon);
    }



    public void setOpdsEntry(OpdsEntryWithStatusCache opdsEntry) {
        this.opdsEntry = opdsEntry;
        ((TextView)findViewById(R.id.item_opds_entry_card_title_text)).setText(opdsEntry.getTitle());

        OpdsEntryStatusCache statusCache = opdsEntry.getStatusCache();
        if(statusCache == null)
            return;

        switch(opdsEntry.getDownloadDisplayState()) {
            case OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_DOWNLOADED:
                statusButton.setImageResource(R.drawable.ic_offline_pin_black_24dp);
                statusButton.setContentDescription(getContext().getResources().getString(R.string.downloaded));
                statusButton.setProgressVisibility(View.GONE);
                break;

            case OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_PAUSED:
                statusButton.setImageResource(R.drawable.ic_pause_black_24dp);
                statusButton.setContentDescription(getContext().getResources().getString(R.string.paused));
                statusButton.setProgressVisibility(View.VISIBLE);
                statusButton.setProgress(opdsEntry.getDownloadCompletePercentage());
                break;

            case OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_IN_PROGRESS:
                statusButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                statusButton.setContentDescription(getContext().getResources().getString(R.string.downloading));
                statusButton.setProgressVisibility(View.VISIBLE);
                statusButton.setProgress(opdsEntry.getDownloadCompletePercentage());
                break;

            case OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_NOT_DOWNLOADED:
                statusButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                statusButton.setContentDescription(getContext().getResources().getString(R.string.download));
                statusButton.setProgressVisibility(View.GONE);
                break;

            case OpdsEntryWithStatusCache.DOWNLOAD_DISPLAY_STATUS_QUEUED:
                statusButton.setImageResource(R.drawable.ic_queue_download_black_24px);
                statusButton.setContentDescription(getContext().getResources().getString(R.string.queued));
                statusButton.setProgressVisibility(View.VISIBLE);
                statusButton.setProgress(opdsEntry.getDownloadCompletePercentage());
                break;
        }
    }

    public OpdsEntryWithRelations getOpdsEntry() {
        return opdsEntry;
    }


    /**
     * set the status on whether a file can be downloaded locally or not
     * @param isAvailable
     */

    public void setLocalAvailableFile(boolean isAvailable){
//        ((TextView)findViewById(R.id.opds_item_detail_text)).setMainText(
//                isAvailable? R.string.file_available_locally : R.string.file_unavailable_locally);

    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int newColor = isSelected() ? R.color.opds_card_pressed : R.color.opds_card_normal;
//        this.setCardBackgroundColor(getContext().getResources().getColor(newColor));
    }

    public void setProgressBarVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
//        findViewById(R.id.opds_item_download_progress_view).setVisibility(visibility);
    }

    /**
     * Set the current progress amount on the entry - a float between 0 and 1
     *
     * @param loaded
     */
    public void setDownloadProgressBarProgress(float loaded) {
//        mDownloadProgressView.setProgress(loaded);
    }

    public void setDownloadProgressStatusText(String statusText) {
//        mDownloadProgressView.setStatusText(statusText);
    }

    /**
     * Set visibility of the caption view - Available Locally/Unavailable Locally
     * @param visible
     */
    public void setFileAvailabilityTextVisibility(boolean visible){
        Log.d("File Availability",String.valueOf(visible));
        int visibility=visible ? View.VISIBLE : View.INVISIBLE;
//        findViewById(R.id.opds_item_detail_text).setVisibility(visibility);

    }

    public void setOPDSEntryOverlay(int overlay) {
//        ImageView statusIconView = findViewById(R.id.opds_item_status_icon);
//        TextView statusText = findViewById(R.id.opds_item_status_text);
//        switch(overlay) {
//            case CatalogPresenter.STATUS_ACQUIRED:
//                findViewById(R.id.opds_item_download_progress_view).setVisibility(View.GONE);
//                findViewById(R.id.opds_item_status_layout).setVisibility(View.VISIBLE);
//                statusIconView.setImageDrawable(ContextCompat.getDrawable(getContext(),
//                        R.drawable.ic_done_black_16dp));
//                statusText.setMainText(R.string.downloaded);
//                break;
//            case CatalogPresenter.STATUS_AVAILABLE_LOCALLY:
//                findViewById(R.id.opds_item_download_progress_view).setVisibility(View.GONE);
//                findViewById(R.id.opds_item_status_layout).setVisibility(View.VISIBLE);
//                statusIconView.setImageDrawable(ContextCompat.getDrawable(getContext(),
//                        R.drawable.ic_nearby_black_24px));
//                statusText.setMainText(R.string.file_available_locally);
//                break;
//            case CatalogPresenter.STATUS_ACQUISITION_IN_PROGRESS:
//            case CatalogPresenter.STATUS_NOT_ACQUIRED:
//                findViewById(R.id.opds_item_status_layout).setVisibility(View.GONE);
//                break;
//
//        }
    }

    public void setThumbnailUrl(final String url, final String mimeType) {
        final ImageView thumbImageView =(ImageView)findViewById(R.id.item_opds_entry_card_thumbnail);
        if(url == null) {
            thumbImageView.setImageResource(android.R.color.transparent);
            return;
        }

        if(currentThumbnailUrl != null && currentThumbnailUrl.equals(url))
            return;//it hasn't changed

        currentThumbnailUrl = url;
        if(UmAndroidImageUtil.isSvg(mimeType)) {
            UmAndroidImageUtil.loadSvgIntoImageView(url, thumbImageView);
        }else {
            Picasso.with(getContext()).load("um-"+url).fit().centerInside().into(thumbImageView);
        }
    }


    public void setProgress(CourseProgress progress) {
//        LearnerProgressView progressViewHolder = (LearnerProgressView)findViewById(R.id.opds_item_learner_progress_holder);
//        switch(progress.getStatus()) {
//            case CourseProgress.STATUS_NOT_STARTED:
//                progressViewHolder.setVisibility(View.GONE);
//                break;
//
//            default:
//                progressViewHolder.setVisibility(View.VISIBLE);
//                progressViewHolder.setProgress(progress);
//                break;
//        }
    }



    public OnClickDownloadListener getOnClickDownloadListener() {
        return onClickDownloadListener;
    }

    public void setOnClickDownloadListener(OnClickDownloadListener onClickDownloadListener) {
        this.onClickDownloadListener = onClickDownloadListener;
        findViewById(R.id.item_opds_entry_card_download_icon).setOnClickListener(this::handleClickDownloadIcon);
    }

    public void handleClickDownloadIcon(View view){
        if(onClickDownloadListener != null)
            onClickDownloadListener.onClickDownload(opdsEntry);
    }

}
