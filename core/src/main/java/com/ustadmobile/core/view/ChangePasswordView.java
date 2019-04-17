package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ChangePassword's View
 */
public interface ChangePasswordView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ChangePassword";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

