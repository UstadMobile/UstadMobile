package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplayDetails

data class ReportDetailUiState(

    val statementListDetails: List<StatementEntityWithDisplayDetails> = emptyList(),

    val chartData: ChartData? = null

)