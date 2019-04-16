package com.ustadmobile.core.view;


/**
 * Core View. Screen is for RecordDropoutDialog's View
 */
public interface RecordDropoutDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "RecordDropoutDialog";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

