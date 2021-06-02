package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI

class ScheduleEditPresenter(context: Any, args: Map<String, String>, view: ScheduleEditView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ScheduleEditView, Schedule>(context, args, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

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

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //view.frequencyOptions = FrequencyOption.values().map { FrequencyMessageIdOption(it, context) }
        view.dayOptions = DayOptions.values().map { DayMessageIdOption(it, context) }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Schedule? {
        val scheduleData = arguments[UstadEditView.ARG_ENTITY_JSON]
        return if(scheduleData != null) {
            safeParse(di, Schedule.serializer(), scheduleData)
        }else {
            Schedule().apply {
                scheduleActive = true
                scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
            }
        }
    }

    override fun handleClickSave(entity: Schedule) {
        //Remove any previous error messages
        view.fromTimeError = null
        view.toTimeError = null

        if(entity.sceduleStartTime == 0L) {
            view.fromTimeError = systemImpl.getString(MessageID.field_required_prompt,
                context)
            return
        }else if(entity.scheduleEndTime == 0L) {
            view.toTimeError = systemImpl.getString(MessageID.field_required_prompt,
                context)
            return
        }else if(entity.scheduleEndTime <= entity.sceduleStartTime) {
            view.toTimeError = systemImpl.getString(MessageID.end_is_before_start_error,
                context)
            return
        }

        finishWithResult(
            safeStringify(di, ListSerializer(Schedule.serializer()), listOf(entity)))
    }
}