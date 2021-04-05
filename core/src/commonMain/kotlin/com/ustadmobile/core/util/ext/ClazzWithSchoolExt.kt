package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithSchool

/**
 * Given a class and an associated school (if any), determine the effective timezone
 */
inline val ClazzWithSchool.effectiveTimeZone: String get() = clazzTimeZone ?: school?.schoolTimeZone ?: "UTC"
