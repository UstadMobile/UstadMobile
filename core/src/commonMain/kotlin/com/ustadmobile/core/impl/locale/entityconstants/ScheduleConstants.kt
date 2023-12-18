package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_DAILY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY

object ScheduleConstants {

    val DAY_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.sunday, Schedule.DAY_SUNDAY),
        MessageIdOption2(MR.strings.monday, Schedule.DAY_MONDAY),
        MessageIdOption2(MR.strings.tuesday, Schedule.DAY_TUESDAY),
        MessageIdOption2(MR.strings.wednesday, Schedule.DAY_WEDNESDAY),
        MessageIdOption2(MR.strings.thursday, Schedule.DAY_THURSDAY),
        MessageIdOption2(MR.strings.friday, Schedule.DAY_FRIDAY),
        MessageIdOption2(MR.strings.saturday, Schedule.DAY_SATURDAY)
    )

    val DAY_MESSAGE_ID_MAP = DAY_MESSAGE_IDS.map {
        it.value to it.stringResource
    }.toMap()

    val SCHEDULE_FREQUENCY_MESSAGE_ID_MAP = mapOf(
        SCHEDULE_FREQUENCY_DAILY to MR.strings.daily,
        SCHEDULE_FREQUENCY_WEEKLY to MR.strings.weekly,
    )


}