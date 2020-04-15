package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.TimeZoneEntity
import java.util.TimeZone

fun TimeZoneEntity.toTimeZone() = TimeZone.getTimeZone(id)
