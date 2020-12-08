package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithSchool

fun ClazzWithSchool?.effectiveTimeZone(fallback: String = "UTC") = this?.clazzTimeZone ?: this?.school?.schoolTimeZone ?: fallback