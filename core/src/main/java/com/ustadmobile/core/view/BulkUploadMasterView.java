package com.ustadmobile.core.view;

import java.util.List;

public interface BulkUploadMasterView extends UstadView{

    String VIEW_NAME="BulkUploadMaster";

    /**
     * Set time zone as list to the activity.
     *
     * @param timeZoneIds   List of time zone ids
     */
    void setTimeZonesList(List<String> timeZoneIds);

    /**
     * Starts the file picker view
     */
    void chooseFileFromDevice();

    void finish();

    /**
     * Parse file
     * @param filePath  Path of the file to parse
     */
    void parseFile(String filePath);

    /**
     * Show a message on the screen.
     * @param message   The message
     */
    void showMessage(String message);

    /**
     * Sets if the bulk upload is in progress or not.
     * @param inProgress    true if bulk upload is in progress
     */
    void setInProgress(boolean inProgress);

    /**
     * Updates the progress of bulk upload on the view. A percentage will be calculated 
     * @param line  The line number
     * @param nlines    The total number of lines
     */
    void updateProgressValue(int line, int nlines);

}
