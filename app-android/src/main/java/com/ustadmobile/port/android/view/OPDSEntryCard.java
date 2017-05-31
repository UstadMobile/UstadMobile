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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;

/**
 * Created by mike on 08/08/15.
 */
public class OPDSEntryCard extends android.support.v7.widget.CardView {

    private UstadJSOPDSEntry entry;

    /**
     * The 100% amount of the progress bar; defined as 100
     */
    public static final int PROGRESS_ENTRY_MAX = 100;

    /**
     * A drawable that represents the status of the entry (e.g. acquired, not acquired, etc)
     */
    private Drawable opdsStatusOverlay;


    public OPDSEntryCard(Context ctx) {
        super(ctx);
    }

    public OPDSEntryCard(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public OPDSEntryCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setOPDSEntry(UstadJSOPDSEntry entry) {
        this.entry = entry;
        ((TextView)findViewById(R.id.opdsitem_title_text)).setText(entry.title);
    }

    public UstadJSOPDSEntry getEntry() {
        return this.entry;
    }

    /**
     * set the status on whether a file can be downloaded locally or not
     * @param isAvailable
     */

    public void setLocalAvailableFile(boolean isAvailable){
        ((TextView)findViewById(R.id.opds_item_detail_text)).setText(
                isAvailable? UstadMobileSystemImpl.getInstance().getString(MessageIDConstants.fileAvailableLocally)
                        : UstadMobileSystemImpl.getInstance().getString(MessageIDConstants.fileUnAvailableLocally));
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int newColor = isSelected() ? R.color.opds_card_pressed : R.color.opds_card_normal;
        this.setCardBackgroundColor(getContext().getResources().getColor(newColor));
    }

    public void setProgressBarVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        findViewById(R.id.opds_item_progressbar).setVisibility(visibility);
    }

    /**
     * Set the current progress amount on the entry - where PROGRESS_ENTRY_MAX is 100%
     *
     * @param loaded
     */
    public void setDownloadProgressBarProgress(int loaded) {
        ((ProgressBar)findViewById(R.id.opds_item_progressbar)).setProgress(loaded);
    }

    /**
     * Set visibility of the caption view - Available Locally/Unavailable Locally
     * @param visible
     */
    public void setFileAvailabilityTextVisibility(boolean visible){
        Log.d("File Availability",String.valueOf(visible));
        int visibility=visible ? View.VISIBLE : View.INVISIBLE;
        findViewById(R.id.opds_item_detail_text).setVisibility(visibility);

    }

    public void setOPDSEntryOverlay(int overlay) {
        switch(overlay) {
            case CatalogController.STATUS_ACQUIRED:
                findViewById(R.id.opds_item_progressbar).setVisibility(View.GONE);
                findViewById(R.id.opds_item_status_layout).setVisibility(View.VISIBLE);
                break;
            case CatalogController.STATUS_ACQUISITION_IN_PROGRESS:
            case CatalogController.STATUS_NOT_ACQUIRED:
                findViewById(R.id.opds_item_status_layout).setVisibility(View.GONE);
                break;

        }
        /*
        if(overlay == CatalogEntryInfo.ACQUISITION_STATUS_ACQUIRED) {
            opdsStatusOverlay = getResources().getDrawable(R.drawable.opds_item_overlay_acquired);
        }else {
            opdsStatusOverlay = null;
        }

        invalidate();
        */
    }

    /**
     * Set the thumbnail for this OPDS entry card
     *
     * @param bitmap Bitmap with thumbnail image
     */
    public void setThumbnail(Bitmap bitmap) {
        ((ImageView)findViewById(R.id.opds_item_thumbnail)).setImageBitmap(bitmap);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(opdsStatusOverlay != null) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            opdsStatusOverlay.setBounds(getLeft(), getTop(), getWidth(), getHeight());
            opdsStatusOverlay.draw(canvas);
        }
    }
}
