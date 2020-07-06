package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.lib.db.entities.Schedule

interface ScheduleEditView: UstadEditView<Schedule> {

    var frequencyOptions: List<ScheduleEditPresenter.FrequencyMessageIdOption>?

    var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>?

    companion object {
        const val ARG_SCHEDULE = "schedule"
    }

}