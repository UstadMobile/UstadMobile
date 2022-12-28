package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ReportFilterWithDisplayDetails
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters

data class ReportEditUiState(

    val report: ReportWithSeriesWithFilters? = null,

    val titleError: String? = null,

    val reportSeriesUiState: ReportSeriesUiState = ReportSeriesUiState(),

    val fieldsEnabled: Boolean = true,

)

data class ReportSeriesUiState(

    val reportSeriesList: List<ReportSeries> = emptyList(),

    val deleteButtonVisible: Boolean = false,

    val filterList: List<ReportFilterWithDisplayDetails> = emptyList(),

)