package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class UMCalendarWithNumEntries : UMCalendar() {

    var numEntries: Int = 0
}
