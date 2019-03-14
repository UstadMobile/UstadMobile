package com.ustadmobile.core.view;


/**
 * Core View. Screen is for RoleAssignmentDetail's View
 */
public interface RoleAssignmentDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "RoleAssignmentDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


}

