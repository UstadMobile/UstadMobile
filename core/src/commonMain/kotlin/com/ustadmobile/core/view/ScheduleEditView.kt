package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Schedule

interface ScheduleEditView: UstadView {

    var schedule: Schedule?

    fun finishWithResult(schedule: Schedule?)

    companion object {
        const val ARG_SCHEDULE = "schedule"
    }

}