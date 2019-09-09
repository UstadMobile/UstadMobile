package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment

/**
 * View responsible for showing a list of classes that can be enrolled and already enrolled
 * usually seen when editing a person and adding more classes to that person.
 * ClazzListEnrollPerson Core View extends Core UstadView. Will be implemented on various implementations.
 */
interface PersonDetailEnrollClazzView : UstadView {

    /**
     * Sets the clazz list in the person's detail to show a list of every class that person is
     * enrolled in.
     *
     * @param clazzEnrollmentListProvider   The class list UmProvider of ClazzWithEnrollment type.
     */
    fun setClazzListProvider(clazzEnrollmentListProvider: DataSource.Factory<Int, ClazzWithEnrollment>)

    /**
     * This will close the activity (and finish it)
     */
    fun finish()

    companion object {

        val VIEW_NAME = "PersonDetailEnrollClazz"
    }


}
