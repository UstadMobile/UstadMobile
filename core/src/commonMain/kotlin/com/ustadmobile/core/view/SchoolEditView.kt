package com.ustadmobile.core.view

import com.ustadmobile.core.controller.SchoolEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar


interface SchoolEditView: UstadEditView<SchoolWithHolidayCalendar> {

    var schoolClazzes : DoorMutableLiveData<List<Clazz>>?
    var genderOptions: List<SchoolEditPresenter.GenderTypeMessageIdOption>?

    companion object {

        const val VIEW_NAME = "SchoolEditView"

    }

}