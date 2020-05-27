package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.util.getSystemTimeInMillis

suspend fun ClazzLogDao.insertTestClazzLog(clazzUid: Long, date: Long = getSystemTimeInMillis()) = ClazzLog().apply {
    clazzLogClazzUid = clazzUid
    logDate = date
    clazzLogUid = insertAsync(this)
}
