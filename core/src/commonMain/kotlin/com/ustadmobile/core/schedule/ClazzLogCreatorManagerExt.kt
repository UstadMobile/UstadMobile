package com.ustadmobile.core.schedule

import com.soywiz.klock.DateTime
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.lib.db.entities.ClazzWithSchool


fun ClazzLogCreatorManager.requestClazzLogCreationForToday(clazz: ClazzWithSchool, endpoint: String) {
    val fromDateTime = DateTime.now().toOffsetByTimezone(clazz.effectiveTimeZone).localMidnight
    requestClazzLogCreation(clazz.clazzUid, endpoint, fromDateTime.utc.unixMillisLong,
        fromDateTime.localEndOfDay.utc.unixMillisLong)
}
