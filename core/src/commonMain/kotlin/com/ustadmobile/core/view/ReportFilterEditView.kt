package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.lib.db.entities.ReportFilter

interface ReportFilterEditView: UstadEditView<ReportFilter>{

    /**
     * the field that it is to be filtered eg. person gender
     */
    var fieldOptions: List<ReportFilterEditPresenter.FieldOption>?

    /**
     * comparision condition eg. equals, greater than, has, has not
     */
    var conditionsOptions: List<ReportFilterEditPresenter.ConditionOption>?

    /**
     *
     */
    var dropDownValueOptions: List<ReportFilterEditPresenter.ValueOption>?


    var valueType: ReportFilterEditPresenter.FilterValueType


}
