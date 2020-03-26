package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.json.Json

class ScheduleEditPresenter(context: Any, args: Map<String, String>, view: ScheduleEditView,
lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl, db: UmAppDatabase,
    repo: UmAppDatabase, activeAccount: DoorLiveData<UmAccount?>)
    : UstadEditPresenter<ScheduleEditView, Schedule>(context, args, view, lifecycleOwner, systemImpl, db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    interface ScheduleEditDoneListener {
        fun onScheduleEditDone(schedule: Schedule, requestCode: Int)
    }

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

        view.frequencyOptions = FrequencyOption.values().map { FrequencyMessageIdOption(it, context) }
        view.dayOptions = DayOptions.values().map { DayMessageIdOption(it, context) }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Schedule? {
        val scheduleData = arguments[ScheduleEditView.ARG_SCHEDULE]
        return if(scheduleData != null) {
            Json.parse(Schedule.serializer(), scheduleData)
        }else {
            Schedule().apply {
                scheduleActive = true
            }
        }
    }

    override fun handleClickSave(entity: Schedule) {
        view.finishWithResult(entity)
    }
}