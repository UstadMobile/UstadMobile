package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.LeavingReason

suspend fun LeavingReasonDao.initPreloadedLeavingReasons() {
    val uidsInserted = findByUidList(LeavingReason.FIXED_UIDS.values.toList())
    val uidsToInsert = LeavingReason.FIXED_UIDS.filter { it.value !in uidsInserted }
    val verbListToInsert = uidsToInsert.map { reason ->
        LeavingReason(reason.value, reason.key)
    }
    replaceList(verbListToInsert)
}