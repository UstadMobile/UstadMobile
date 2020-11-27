package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.lib.db.entities.Schedule

interface ScheduleEditView: UstadEditView<Schedule> {

    var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>?

    var fromTimeError: String?

    var toTimeError: String?

    companion object {
        const val ARG_SCHEDULE = "schedule"
    }

}