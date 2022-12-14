package com.ustadmobile.core.db.dao

import com.ustadmobile.door.util.systemTimeInMillis

suspend fun ClazzEnrolmentDao.updateDateLeft(clazzEnrolmentUidList: List<Long>, endDate: Long) {
    val updateTime = systemTimeInMillis()
    clazzEnrolmentUidList.forEach {
        updateDateLeftByUid(it, endDate, updateTime)
    }
}