package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

/**
 * Core View. Screen is for GroupDetail's View
 */
interface GroupDetailView : UstadView {

    fun setListProvider(provider: DataSource.Factory<Int, PersonWithEnrollment>)

    fun updateGroupOnView(group: PersonGroup)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "GroupDetail"

        //Any argument keys:
        val GROUP_UID = "GroupUid"
    }


}

