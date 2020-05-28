package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.lib.db.entities.*

fun UmAppDatabase.runPreload() {
    preload()
    timeZoneEntityDao.insertSystemTimezones()
    personDetailPresenterFieldDao.preloadCoreFields()
}

/**
 * Enrol the given person into the given class. The effective date of joining is midnight as per
 * the timezone of the class (e.g. when a teacher adds a student to the system who just joined and
 * wants to mark their attendance for the same day).
 */
suspend fun UmAppDatabase.enrolPersonIntoClazzAtLocalTimezone(personToEnrol: Person, clazzUid: Long,
                                                              role: Int,
                                                              clazzWithSchool: ClazzWithSchool? = null): ClazzMemberWithPerson {
    val clazzTimeZone = (clazzWithSchool ?: clazzDao.getClazzWithSchool(clazzUid))?.effectiveTimeZone() ?: "UTC"
    val joinTime = DateTime.now().toOffsetByTimezone(clazzTimeZone).utc.unixMillisLong
    return ClazzMemberWithPerson().apply {
        clazzMemberPersonUid = personToEnrol.personUid
        clazzMemberClazzUid = clazzUid
        clazzMemberRole = role
        clazzMemberActive = true
        clazzMemberDateJoined = joinTime
        person = personToEnrol
        clazzMemberUid = clazzMemberDao.insertAsync(this)
    }
}
