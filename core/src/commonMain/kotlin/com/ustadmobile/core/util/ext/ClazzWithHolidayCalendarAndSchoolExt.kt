package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology

inline val ClazzWithHolidayCalendarAndSchoolAndTerminology.effectiveTimeZone: String get() = clazzTimeZone ?: "UTC"