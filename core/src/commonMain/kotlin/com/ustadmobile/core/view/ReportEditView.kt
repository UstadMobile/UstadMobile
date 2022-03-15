package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.util.ObjectMessageIdOption
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters


interface ReportEditView: UstadEditView<ReportWithSeriesWithFilters> {

    var visualTypeOptions: List<ReportEditPresenter.VisualTypeMessageIdOption>?
    var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>?
    var subGroupOptions: List<ReportEditPresenter.SubGroupByMessageIdOption>?
    var yAxisOptions: List<ReportEditPresenter.YAxisMessageIdOption>?
    var dateRangeOptions: List<ObjectMessageIdOption<DateRangeMoment>>?
    var selectedDateRangeMoment: DateRangeMoment?

    var titleErrorText: String?

    companion object {

        const val VIEW_NAME = "ReportEditView"

    }

}