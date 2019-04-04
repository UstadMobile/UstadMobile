package com.ustadmobile.core.view;


/**
 * Core View. Screen is for BasePoint2View's View
 */
public interface BasePoint2View extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "BasePoint2View";

    //Any argument keys:


    void showCatalog(boolean show);
    void showInventory(boolean show);
    void showSales(boolean show);
    void showCourses(boolean show);
    void shareAppSetupFile(String filePath);
    void forceSync();
    void sendToast(int messageId);
    void checkPermissions();

    /**
     * Show a dialog used to share the application itself offline
     */
    void showShareAppDialog();

    /**
     * dismiss the share app dialog
     */
    void dismissShareAppDialog();

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

