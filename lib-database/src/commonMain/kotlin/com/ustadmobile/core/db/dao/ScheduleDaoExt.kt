package com.ustadmobile.core.db.dao

suspend fun ScheduleDao.deactivateByUids(uidList: List<Long>, changeTime: Long) {
    uidList.forEach { updateScheduleActivated(it, false, changeTime) }
}
