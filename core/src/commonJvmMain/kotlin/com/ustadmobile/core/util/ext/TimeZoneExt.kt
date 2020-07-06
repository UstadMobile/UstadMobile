package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.TimeZoneEntity
import java.util.TimeZone

fun TimeZone.asTimeZoneEntity(): TimeZoneEntity {
    return TimeZoneEntity(this.id, this.rawOffset)
}
