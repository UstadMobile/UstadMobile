package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithSchool

fun ClazzWithSchool.effectiveTimeZone() = clazzTimeZone ?: school?.schoolTimeZone ?: "UTC"