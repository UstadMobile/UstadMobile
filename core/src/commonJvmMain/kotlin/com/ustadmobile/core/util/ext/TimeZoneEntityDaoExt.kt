package com.ustadmobile.core.util.ext
import com.ustadmobile.core.db.dao.TimeZoneEntityDao
import com.ustadmobile.lib.db.entities.TimeZoneEntity
import java.util.TimeZone


actual fun TimeZoneEntityDao.insertSystemTimezones() {
    val timeZoneList = TimeZone.getAvailableIDs().map { TimeZone.getTimeZone(it) }
            .sortedBy { it.rawOffset }
    replaceList(timeZoneList.map {it.asTimeZoneEntity()} )
}



