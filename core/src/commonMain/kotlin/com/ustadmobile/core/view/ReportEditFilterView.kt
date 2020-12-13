package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportEditFilterPresenter
import com.ustadmobile.lib.db.entities.ReportFilter

interface ReportEditFilterView: UstadEditView<ReportFilter>{

    var conditionsOptions: List<ReportEditFilterPresenter.ConditionTypeOption>?
    var valueOptions: List<ReportEditFilterPresenter.ValueTypeOption>?
    var fieldOptions: List<ReportEditFilterPresenter.ValueTypeOption>?

}
