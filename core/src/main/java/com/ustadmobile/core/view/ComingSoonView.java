package com.ustadmobile.core.view;


/**
 * Core View. Screen is for ComingSoon's View
 */
public interface ComingSoonView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "ComingSoon";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

