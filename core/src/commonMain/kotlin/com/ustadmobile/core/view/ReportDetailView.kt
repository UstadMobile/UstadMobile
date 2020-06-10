package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.util.ReportGraphHelper
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.lib.db.entities.StatementListReport


interface ReportDetailView: UstadDetailView<ReportWithFilters> {

    var statementList: DataSource.Factory<Int, StatementListReport>?

    var chartData: DoorMutableLiveData<ReportGraphHelper.ChartData>?

    companion object {

        const val VIEW_NAME = "ReportDetailView"

    }

}