package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation

interface SchoolListView: UstadListView<School, SchoolWithMemberCountAndLocation> {

    companion object {
        const val VIEW_NAME = "SchoolListView"
    }

}