package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ConfirmLogoutDialog's View
 */
public interface ConfirmLogoutDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ConfirmLogoutDialog";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

