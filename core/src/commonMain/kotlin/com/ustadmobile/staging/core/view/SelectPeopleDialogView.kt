package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

interface SelectPeopleDialogView : UstadView {
    fun finish()

    fun setPeopleProvider(peopleListProvider: DataSource.Factory<Int, PersonWithEnrollment>)

    companion object {

        val VIEW_NAME = "SelectPeopleDialogView"
        val ARG_SELECTED_PEOPLE = "SelectedPeople"
        val ARG_SELECT_ACTOR = "SelectActor"
    }
}
