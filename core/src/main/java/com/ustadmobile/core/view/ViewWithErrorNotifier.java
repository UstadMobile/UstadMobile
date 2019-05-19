package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ViewWithErrorNotifier's View
 */
public interface ViewWithErrorNotifier extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ViewWithErrorNotifier";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();

    void showErrorNotification(String errorMessage, Runnable action, int actionMessageId);


}

