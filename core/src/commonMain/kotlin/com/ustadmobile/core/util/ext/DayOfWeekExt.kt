package com.ustadmobile.core.util.ext

import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.DayOfWeek
import com.ustadmobile.core.MR

val DayOfWeek.dayStringResource: StringResource
    get() = when(this) {
        DayOfWeek.MONDAY -> MR.strings.monday
        DayOfWeek.TUESDAY -> MR.strings.tuesday
        DayOfWeek.WEDNESDAY -> MR.strings.wednesday
        DayOfWeek.THURSDAY -> MR.strings.thursday
        DayOfWeek.FRIDAY -> MR.strings.friday
        DayOfWeek.SATURDAY -> MR.strings.saturday
        DayOfWeek.SUNDAY -> MR.strings.sunday
    }
