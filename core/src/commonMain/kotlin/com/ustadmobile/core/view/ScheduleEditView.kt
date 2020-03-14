package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.lib.db.entities.Schedule

interface ScheduleEditView: UstadView {

    var schedule: Schedule?

    var frequencyOptions: List<ScheduleEditPresenter.FrequencyMessageIdOption>?

    var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>?

    fun finishWithResult(schedule: Schedule)

    companion object {
        const val ARG_SCHEDULE = "schedule"
    }

}