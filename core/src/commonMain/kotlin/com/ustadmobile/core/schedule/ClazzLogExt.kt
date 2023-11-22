package com.ustadmobile.core.schedule

import com.ustadmobile.lib.db.entities.ClazzLog

/**
 * The ClazzLog may be generated on the server and multiple devices simultaneously. We therefor
 * need to ensure that each device independently generates exactly the same UID to avoid duplicates.
 */
fun ClazzLog.generateUid(): Long {
    return (clazzLogClazzUid.hashCode().toLong() shl 32) or (logDate.hashCode().toLong())
}

val ClazzLog.totalAttendeeStatusRecorded: Int
    get() =  clazzLogNumPresent + clazzLogNumPartial + clazzLogNumAbsent


