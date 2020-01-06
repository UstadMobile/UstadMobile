package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Person


/**
 * Core View. Screen is for SelectPersonDialog's View
 */
interface SelectCustomerView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, Person>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectPersonDialog"
        const val ARG_SP_LE_UID = "LeUid";
    }


}

