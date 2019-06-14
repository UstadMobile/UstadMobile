package com.ustadmobile.core.view;


/**
 * Core View. Screen is for AddReminderDialog's View
 */
public interface AddReminderDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AddReminderDialog";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();

}

