package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.Report

fun ReportDao.initPreloadedTemplates() {
    val uidsInserted = findByUidList(Report.FIXED_TEMPLATES.map { it.reportUid })
    val templateListToInsert = Report.FIXED_TEMPLATES.filter { it.reportUid !in uidsInserted }
    replaceList(templateListToInsert)
}
