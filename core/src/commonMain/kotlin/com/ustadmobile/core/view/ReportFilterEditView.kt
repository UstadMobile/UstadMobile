package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel

interface ReportFilterEditView: UstadEditView<ReportFilter>{

    /**
     * the field that it is to be filtered eg. person gender
     */
    var fieldOptions: List<ReportFilterEditPresenter.FieldMessageIdOption>?

    /**
     * comparision condition eg. equals, greater than, has, has not
     */
    var conditionsOptions: List<ReportFilterEditPresenter.ConditionMessageIdOption>?

    /**
     *
     */
    var dropDownValueOptions: List<MessageIdOption>?


    var valueType: ReportFilterEditPresenter.FilterValueType?

    var fieldErrorText: String?

    var conditionsErrorText: String?

    var valuesErrorText: String?

    var uidAndLabelList: LiveData<List<UidAndLabel>>?

    var createNewFilter: String?

    companion object {

        const val VIEW_NAME = "ReportFilterEditView"

    }


}
