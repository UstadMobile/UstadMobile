package com.ustadmobile.core.schedule

import com.ustadmobile.core.impl.UmAccountManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


actual fun requestClazzLogCreation(clazzUidFilter: Long, dbName: String, fromTime: Long, toTime: Long,
                                   context: Any) {
    GlobalScope.launch {
        UmAccountManager.getRepositoryForActiveAccount(context).createClazzLogs(
                fromTime, toTime, clazzUidFilter, false)
    }
}