package com.ustadmobile.core.view;

/**
 * Created by mike on 4/17/17.
 */

public interface CatalogEntryView extends UstadView {

    int MODE_ENTRY_DOWNLOADABLE = 0;

    int MODE_ENTRY_DOWNLOADED = 1;



    int BUTTON_DOWNLOAD = 0;

    int BUTTON_REMOVE = 1;

    int BUTTON_OPEN = 2;

    int LOCAL_STATUS_IN_PROGRESS  = 0;

    int LOCAL_STATUS_NOT_AVAILABLE = 1;

    int LOCAL_STATUS_AVAILABLE = 2;




    String VIEW_NAME = "CatalogEntry";

    /**
     * Set which buttons are visible or not :
     *  When the item is not yet downloaded only the download button is visible
     *  When the item is downloaded remove/modify is available and open is available.
     *
     * @param buttonId
     * @param display
     */
    void setButtonDisplayed(int buttonId, boolean display);

    void setHeader(String headerFileUri);

    void setIcon(String iconFileUri);

    void setMode(int mode);

    void setLocallyAvailableStatus(int status);

    void setSize(long downloadSize);

    void setDescription(String description, String contentType);

    void setTitle(String title);

    /**
     * Sets whether or not the progress section of the view (progress bar, status text etc) are
     * visible
     * @param visible
     */
    void setProgressVisible(boolean visible);

    void setProgress(float progress);

    void setProgressStatusText(String progressStatusText);
}
