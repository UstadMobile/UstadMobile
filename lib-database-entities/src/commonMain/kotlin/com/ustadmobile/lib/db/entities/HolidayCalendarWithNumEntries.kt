package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class HolidayCalendarWithNumEntries : HolidayCalendar() {

    var numEntries: Int = 0
}
