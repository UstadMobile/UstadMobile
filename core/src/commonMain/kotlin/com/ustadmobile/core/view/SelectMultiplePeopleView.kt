package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Person


/**
 * Core View. Screen is for SelectMultiplePeople's View
 */
interface SelectMultiplePeopleView : UstadView {

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
        val VIEW_NAME = "SelectMultiplePeople"

        //Any argument keys:
        val ARG_SELECTED_PEOPLE = "ArgSelectedPeople"
    }


}

