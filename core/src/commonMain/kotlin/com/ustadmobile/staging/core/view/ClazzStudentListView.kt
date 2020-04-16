package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

/**
 * ClassStudentList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzStudentListView : UstadView {

    /**
     * This methods purpose is to set the provider given to it to the view.
     * On Android it will be set to the Recycler View
     *
     * @param setPersonUmProvider  The provider data
     */
    fun setPersonWithEnrollmentProvider(setPersonUmProvider: DataSource.Factory<Int, PersonWithEnrollment>)


    /**
     * Sorts the sorting drop down (spinner) for sort options in the Class Student list view.
     *
     * @param presets A String array String[] of the presets available.
     */
    fun updateSortSpinner(presets: Array<String?>)

    companion object {

        val VIEW_NAME = "ClassStudentList"
    }

}
