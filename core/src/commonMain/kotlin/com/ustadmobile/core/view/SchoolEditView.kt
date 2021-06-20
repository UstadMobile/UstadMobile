package com.ustadmobile.core.view

import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar


interface SchoolEditView: UstadEditView<SchoolWithHolidayCalendar> {

    companion object {

        const val VIEW_NAME = "SchoolEdit"

    }

}