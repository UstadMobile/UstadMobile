package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.Role;

/**
 * Core View. Screen is for RoleDetail's View
 */
public interface RoleDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "RoleDetail";

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    void finish();


    void updateRoleOnView(Role role);
}

