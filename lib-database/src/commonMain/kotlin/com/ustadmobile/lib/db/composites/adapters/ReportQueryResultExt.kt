package com.ustadmobile.lib.db.composites.adapters

import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.lib.db.entities.ReportQueryResult

fun ReportQueryResult.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)
