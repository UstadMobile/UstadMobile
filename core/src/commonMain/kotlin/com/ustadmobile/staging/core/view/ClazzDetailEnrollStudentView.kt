package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

/**
 * ClazzDetailEnrollStudentView Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzDetailEnrollStudentView : UstadView {

    /**
     * Method to set the Person provider.
     *
     * @param studentsProvider The provider of type PersonWithEnrollment
     */
    fun setStudentsProvider(studentsProvider: DataSource.Factory<Int, PersonWithEnrollment>)

    /**
     * Finish activity: Close it.
     */
    fun finish()

    companion object {

        //The View name
        val VIEW_NAME = "ClazzDetailEnrollStudent"

        // ARGUMENT TO VIEWS THAT DENOTES that this is a new person.
        val ARG_NEW_PERSON = "argNewPerson"

        //ARG to see if adding a teacher or a student
        val ARG_NEW_PERSON_TYPE = "argNewPersonType"
    }

}
