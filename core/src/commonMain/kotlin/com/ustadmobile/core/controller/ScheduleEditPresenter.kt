package com.ustadmobile.core.controller

import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.serialization.json.Json

class ScheduleEditPresenter(context: Any, args: Map<String, String>, view: ScheduleEditView) : UstadBaseController<ScheduleEditView>(context, args, view) {

    var schedule: Schedule? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val scheduleData = arguments[ScheduleEditView.ARG_SCHEDULE]
        schedule = if(scheduleData != null) {
            Json.parse(Schedule.serializer(), scheduleData)
        }else {
            Schedule()
        }

        view.schedule = schedule
    }

    fun handleClickDone(schedule: Schedule) {
        view.finishWithResult(schedule)
    }


}