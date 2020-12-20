package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*


interface ReportEditView: UstadEditView<ReportWithFilters> {

    var seriesLiveData: DoorMutableLiveData<List<ReportSeries>>?

    var visualTypeOptions: List<ReportEditPresenter.VisualTypeMessageIdOption>?
    var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>?
    var subGroupOptions: List<ReportEditPresenter.SubGroupByMessageIdOption>?
    var dataSetOptions: List<ReportEditPresenter.DataSetMessageIdOption>?

    var titleErrorText: String?

    companion object {

        const val VIEW_NAME = "ReportEditEditView"

    }

}