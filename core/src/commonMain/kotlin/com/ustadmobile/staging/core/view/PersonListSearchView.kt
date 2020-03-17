package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

interface PersonListSearchView : UstadView {
    /**
     * Set people list provider to the view.
     *
     * @param listProvider  The people list of PersonWithEnrollment type
     */
    fun setPeopleListProvider(listProvider: DataSource.Factory<Int, PersonWithEnrollment>)

    companion object {

        val VIEW_NAME = "PersonListSearch"

        val ARGUMENT_CURRNET_CLAZZ_UID = "PersonListSearchCurrentclazzUid"
    }
}
