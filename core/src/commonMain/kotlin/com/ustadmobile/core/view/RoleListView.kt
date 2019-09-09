package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Role


/**
 * Core View. Screen is for RoleList's View
 */
interface RoleListView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, Role>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "RoleList"

        //Any argument keys:
        val ROLE_UID = "RoleUid"
    }


}

