package com.ustadmobile.core.view;

/**
 * Core view for main application base point - show bottom navigation and its items
 */
public interface BasePointView2 extends UstadView {

    String VIEW_NAME = "PeopleHome";

    /**
     * Show a dialog used to share the application itself offline
     */
    void showShareAppDialog();

    void dismissShareAppDialog();

    void shareAppSetupFile(String filePath);

    /**
     * Shows the bulk upload button if logged-in person is admin or not depending on the parameter.
     * @param show  boolean argument. if true, the bulk master button will show up. if false it will
     *              hide it.
     */
    void showBulkUploadForAdmin(boolean show);

    void showSettings(boolean show);

    void updatePermissionCheck();

    void forceSync();
}
