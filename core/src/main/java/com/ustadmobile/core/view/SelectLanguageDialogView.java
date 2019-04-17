package com.ustadmobile.core.view;


/**
 * Core View. Screen is for SelectLanguageDialog's View
 */
public interface SelectLanguageDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SelectLanguageDialog";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

