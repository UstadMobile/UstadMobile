package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplayDetails

data class ReportDetailUiState(

    val statementListDetails: List<StatementEntityWithDisplayDetails> = emptyList(),

    val chart: ChartData? = null,

    val saveAsTemplateVisible: Boolean = false,

) {

    val addToDashboardVisible: Boolean
        get() = chart?.reportWithFilters?.reportUid == 0L

}