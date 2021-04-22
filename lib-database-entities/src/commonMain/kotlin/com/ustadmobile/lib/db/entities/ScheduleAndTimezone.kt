package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ScheduleAndTimezone {

    @Embedded
    var schedule: Schedule? = null

    var scheduleTimeZone: String? = null

}