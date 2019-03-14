package com.ustadmobile.core.view;


/**
 * Core View. Screen is for AuditLogSelection's View
 */
public interface AuditLogSelectionView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AuditLogSelection";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

