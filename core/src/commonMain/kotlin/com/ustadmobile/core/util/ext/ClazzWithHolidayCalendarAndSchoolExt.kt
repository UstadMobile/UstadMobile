package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool

inline val ClazzWithHolidayCalendarAndSchool.effectiveTimeZone: String get() = clazzTimeZone ?: school?.schoolTimeZone ?: "UTC"