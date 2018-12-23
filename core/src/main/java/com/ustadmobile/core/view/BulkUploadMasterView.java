package com.ustadmobile.core.view;

public interface BulkUploadMasterView extends UstadView{

    String VIEW_NAME="BulkUploadMaster";

    /**
     * Starts the file picker view
     */
    void chooseFileFromDevice();

    void finish();

    void parseFile(String filePath);

    void showMessage(String message);

    void setInProgress(boolean inProgress);

    void updateProgressValue(int line, int nlines);

}
