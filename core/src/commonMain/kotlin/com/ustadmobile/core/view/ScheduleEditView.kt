package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Schedule

interface ScheduleEditView: UstadEditView<Schedule> {


    var fromTimeError: String?

    var toTimeError: String?

    companion object {

        const val VIEW_NAME = "ScheduleEdit"

        const val ARG_SCHEDULE = "schedule"
    }

}