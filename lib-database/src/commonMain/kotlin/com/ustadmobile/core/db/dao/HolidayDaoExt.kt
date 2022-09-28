package com.ustadmobile.core.db.dao

suspend fun HolidayDao.deactivateByUids(uidList: List<Long>, changeTime: Long) {
    uidList.forEach { updateActiveByUid(it, false, changeTime) }
}