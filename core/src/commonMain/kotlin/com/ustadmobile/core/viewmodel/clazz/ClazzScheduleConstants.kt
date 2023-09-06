package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_DAILY
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_FRIDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_MONDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_SATURDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_SUNDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_THURSDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_TUESDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_WEDNESDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY

object ClazzScheduleConstants {

    val SCHEDULE_FREQUENCY_STRING_RESOURCES = mapOf(
        SCHEDULE_FREQUENCY_DAILY to  MR.strings.daily,
        SCHEDULE_FREQUENCY_WEEKLY to MR.strings.weekly,
    )

    val DAY_STRING_RESOURCES = mapOf(
        DAY_MONDAY to MR.strings.monday,
        DAY_TUESDAY to MR.strings.tuesday,
        DAY_WEDNESDAY to MR.strings.wednesday,
        DAY_THURSDAY to MR.strings.thursday,
        DAY_FRIDAY to MR.strings.friday,
        DAY_SATURDAY to MR.strings.saturday,
        DAY_SUNDAY to MR.strings.sunday,
    )



}