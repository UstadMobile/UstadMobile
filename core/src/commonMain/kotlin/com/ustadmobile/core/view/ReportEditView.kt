package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*


interface ReportEditView: UstadEditView<ReportWithFilters> {

    var personFilterList : DoorMutableLiveData<List<ReportFilterWithDisplayDetails>>?
    var verbFilterList : DoorMutableLiveData<List<ReportFilterWithDisplayDetails>>?
    var contentFilterList: DoorMutableLiveData<List<ReportFilterWithDisplayDetails>>?

    var chartOptions: List<ReportEditPresenter.ChartTypeMessageIdOption>?
    var yAxisOptions: List<ReportEditPresenter.YAxisMessageIdOption>?
    var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>?
    var groupOptions: List<ReportEditPresenter.GroupByMessageIdOption>?

    var titleErrorText: String?

    companion object {

        const val VIEW_NAME = "ReportEditEditView"

    }

}