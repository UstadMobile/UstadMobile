package com.ustadmobile.core.view


import com.ustadmobile.lib.db.entities.Role

/**
 * Core View. Screen is for RoleDetail's View
 */
interface RoleDetailView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    fun updateRoleOnView(role: Role)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "RoleDetail"
    }
}

