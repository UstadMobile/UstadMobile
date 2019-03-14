package com.ustadmobile.core.view;


/**
 * Core View. Screen is for GroupDetail's View
 */
public interface GroupDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "GroupDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

