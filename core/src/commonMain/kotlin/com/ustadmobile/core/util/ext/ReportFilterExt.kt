package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ReportFilter

fun ReportFilter.toDisplayString(context: Any): String {
    val fieldOption = ReportFilterEditPresenter.FieldOption.values()
            .find { it.optionVal == reportFilterField } ?: return ""
    val conditionOption = ReportFilterEditPresenter.ConditionOption.values()
            .find { it.optionVal == reportFilterCondition } ?: return ""

    val fieldValue = UstadMobileSystemImpl.instance.getString(fieldOption.messageId, context)
    val conditionValue = UstadMobileSystemImpl.instance.getString(conditionOption.messageId, context)

    var valueString = reportFilterValue
    if(reportFilterValue.isNullOrBlank()){
        val selectedOption = ReportFilterEditPresenter.genderMap.keys
                .find { it == reportFilterDropDownValue }
        val selectedMessagedId =  ReportFilterEditPresenter.genderMap[selectedOption] ?: 0
        valueString = UstadMobileSystemImpl.instance.getString(selectedMessagedId, context)
    }

    return "$fieldValue $conditionValue $valueString"
}