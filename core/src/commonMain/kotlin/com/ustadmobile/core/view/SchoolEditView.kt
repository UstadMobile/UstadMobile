package com.ustadmobile.core.view

import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ScopedGrantAndName


interface SchoolEditView: UstadEditView<SchoolWithHolidayCalendar> {

    var scopedGrants: LiveData<List<ScopedGrantAndName>>?

    companion object {

        const val VIEW_NAME = "InstitutionEditView"

    }

}