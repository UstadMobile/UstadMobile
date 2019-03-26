package com.ustadmobile.core.view;


/**
 * Core View. Screen is for AddCustomFieldOptionDialogView's View
 */
public interface AddCustomFieldOptionDialogView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AddCustomFieldOptionDialogView";

    //Any argument keys:
    String ARG_CUSTOM_FIELD_VALUE_OPTION_UID = "CustomFieldOptionUid";
    /**
     * Method to finish the screen / view.
     */
    void finish();


}

