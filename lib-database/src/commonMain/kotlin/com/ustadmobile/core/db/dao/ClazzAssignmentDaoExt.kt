package com.ustadmobile.core.db.dao

suspend fun ClazzAssignmentDao.deactivateByUids(uidList: List<Long>, changeTime: Long) {
    uidList.forEach {
        updateActiveByUid(it, false, changeTime)
    }
}
