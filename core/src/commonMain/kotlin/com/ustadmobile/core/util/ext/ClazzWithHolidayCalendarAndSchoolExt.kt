package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndAndTerminology

inline val ClazzWithHolidayCalendarAndAndTerminology.effectiveTimeZone: String get() = clazzTimeZone ?: "UTC"