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
}
