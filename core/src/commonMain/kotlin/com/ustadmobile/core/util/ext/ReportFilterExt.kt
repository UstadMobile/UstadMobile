package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ReportFilter

fun ReportFilter.toDisplayString(systemImpl: UstadMobileSystemImpl, context: Any): String {
    val fieldOption = ReportFilterEditPresenter.FieldOption.values()
            .find { it.optionVal == reportFilterField } ?: return ""
    val conditionOption = ReportFilterEditPresenter.ConditionOption.values()
            .find { it.optionVal == reportFilterCondition } ?: return ""

    val fieldValue = systemImpl.getString(fieldOption.messageId, context)
    val conditionValue = systemImpl.getString(conditionOption.messageId, context)

    var valueString = reportFilterValue
    when {
        ReportFilter.FIELD_PERSON_GENDER == reportFilterField -> {

            val selectedOption = ReportFilterEditPresenter.genderMap.entries.find {
                it.key == reportFilterDropDownValue
            }
            valueString = systemImpl.getString(
                    selectedOption?.component2() ?: 0, context)
        }
        ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME == reportFilterField -> {
            val selectedOption = OUTCOME_TO_MESSAGE_ID_MAP.entries.find {
                it.key == reportFilterDropDownValue
            }
            valueString = systemImpl.getString(
                    selectedOption?.component2() ?: 0, context)
        }
        ReportFilter.FIELD_CONTENT_COMPLETION == reportFilterField -> {
            val selectedOption = ReportFilterEditPresenter.ContentCompletionStatusOption.values()
                    .find { it.optionVal == reportFilterDropDownValue }
            valueString = systemImpl.getString(
                    selectedOption?.messageId ?: 0, context)
        }
        reportFilterCondition == ReportFilter.CONDITION_BETWEEN -> {
            valueString = """$reportFilterValueBetweenX ${systemImpl.getString(MessageID.and, context)} $reportFilterValueBetweenY"""
        }
        reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY || reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON -> {
           valueString = "..."
        }
    }

    return "$fieldValue $conditionValue $valueString"
}