package com.ustadmobile.core.db.dao

import com.ustadmobile.door.SimpleDoorQuery
import com.ustadmobile.lib.db.entities.StatementReportData

suspend fun StatementDao.getResults(sqlStr: String, paramsList: Array<Any>): List<StatementReportData> {
    return getResults(SimpleDoorQuery(sqlStr, paramsList))
}