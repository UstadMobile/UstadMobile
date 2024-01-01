package com.ustadmobile.core.util.ext

import org.quartz.JobDataMap

fun JobDataMap.getIntOrNull(key: String): Int? {
    return if(containsKey(key)) getInt(key) else null
}
