package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_DAILY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY

object ScheduleConstants {

    val DAY_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.sunday, Schedule.DAY_SUNDAY),
        MessageIdOption2(MessageID.monday, Schedule.DAY_MONDAY),
        MessageIdOption2(MessageID.tuesday, Schedule.DAY_TUESDAY),
        MessageIdOption2(MessageID.wednesday, Schedule.DAY_WEDNESDAY),
        MessageIdOption2(MessageID.thursday, Schedule.DAY_THURSDAY),
        MessageIdOption2(MessageID.friday, Schedule.DAY_FRIDAY),
        MessageIdOption2(MessageID.saturday, Schedule.DAY_SATURDAY)
    )

    val DAY_MESSAGE_ID_MAP = DAY_MESSAGE_IDS.map {
        it.value to it.messageId
    }.toMap()

    val SCHEDULE_FREQUENCY_MESSAGE_ID_MAP = mapOf(
        SCHEDULE_FREQUENCY_DAILY to MessageID.daily,
        SCHEDULE_FREQUENCY_WEEKLY to MessageID.weekly,
    )


}