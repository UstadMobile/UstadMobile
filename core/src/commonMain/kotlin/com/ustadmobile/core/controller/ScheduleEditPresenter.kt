package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.serialization.json.Json

class ScheduleEditPresenter(context: Any, args: Map<String, String>, view: ScheduleEditView) : UstadBaseController<ScheduleEditView>(context, args, view) {

    enum class FrequencyOption(val optionVal: Int, val messageId: Int) {
        DAILY(Schedule.SCHEDULE_FREQUENCY_DAILY, MessageID.daily),
        WEEKLY(Schedule.SCHEDULE_FREQUENCY_WEEKLY, MessageID.weekly)
    }

    class FrequencyMessageIdOption(frequency: FrequencyOption, context: Any) : MessageIdOption(frequency.messageId, context, frequency.optionVal)

    enum class DayOptions(val optionVal: Int, val messageId: Int) {
        SUNDAY(Schedule.DAY_SUNDAY, MessageID.sunday),
        MONDAY(Schedule.DAY_MONDAY, MessageID.monday),
        TUESDAY(Schedule.DAY_TUESDAY, MessageID.tuesday),
        WEDNESDAY(Schedule.DAY_WEDNESDAY, MessageID.wednesday),
        THURSDAY(Schedule.DAY_THURSDAY, MessageID.thursday),
        FRIDAY(Schedule.DAY_FRIDAY, MessageID.friday),
        SATURDAY(Schedule.DAY_SATURDAY, MessageID.saturday)
    }

    class DayMessageIdOption(day: DayOptions, context: Any) : MessageIdOption(day.messageId, context, day.optionVal)

    var schedule: Schedule? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val scheduleData = arguments[ScheduleEditView.ARG_SCHEDULE]
        schedule = if(scheduleData != null) {
            Json.parse(Schedule.serializer(), scheduleData)
        }else {
            Schedule()
        }

        view.frequencyOptions = FrequencyOption.values().map { FrequencyMessageIdOption(it, context) }
        view.schedule = schedule
        view.dayOptions = DayOptions.values().map { DayMessageIdOption(it, context) }
    }

    fun handleClickDone(schedule: Schedule) {
        view.finishWithResult(schedule)
    }


}