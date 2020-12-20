package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.lib.db.entities.*


interface ReportEditView: UstadEditView<ReportWithSeriesWithFilters> {

    var visualTypeOptions: List<ReportEditPresenter.VisualTypeMessageIdOption>?
    var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>?
    var subGroupOptions: List<ReportEditPresenter.SubGroupByMessageIdOption>?
    var dataSetOptions: List<ReportEditPresenter.DataSetMessageIdOption>?

    var titleErrorText: String?

    companion object {

        const val VIEW_NAME = "ReportEditEditView"

    }

}