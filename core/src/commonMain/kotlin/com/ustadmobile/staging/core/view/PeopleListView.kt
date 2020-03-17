package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

/**
 * PeopleList is the core view responsible for showing all people in a list.
 * PeopleList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface PeopleListView : UstadView {

    /**
     * Set people list provider to the view.
     *
     * @param listProvider  The people list of PersonWithEnrollment type
     */
    fun setPeopleListProvider(listProvider: DataSource.Factory<Int, PersonWithEnrollment>)

    fun showFAB(show: Boolean)

    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    fun updateSortSpinner(presets: Array<String?>)

    companion object {

        val VIEW_NAME = "PeopleList"

        val SORT_ORDER_NAME_ASC = 1
        val SORT_ORDER_NAME_DESC = 2
        val SORT_ORDER_ATTENDANCE_ASC = 3
        val SORT_ORDER_ATTENDANCE_DESC = 4
    }
}
