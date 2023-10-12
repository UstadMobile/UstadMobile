package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ReportFilter

fun ReportFilter.toDisplayString(systemImpl: UstadMobileSystemImpl, context: Any): String {
    val fieldOption = ReportFilterEditPresenter.FieldOption.values()
            .find { it.optionVal == reportFilterField } ?: return ""
    val conditionOption = ReportFilterEditPresenter.ConditionOption.values()
            .find { it.optionVal == reportFilterCondition } ?: return ""

    val fieldValue = systemImpl.getString(fieldOption.stringResource)
    val conditionValue = systemImpl.getString(conditionOption.stringResource)

    var valueString = reportFilterValue
    when {
        ReportFilter.FIELD_PERSON_GENDER == reportFilterField -> {

            val selectedOption = ReportFilterEditPresenter.genderMap.entries.find {
                it.key == reportFilterDropDownValue
            }
            valueString = systemImpl.getString(
                    selectedOption?.component2() ?: MR.strings.unset)
        }
        ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME == reportFilterField -> {
            val selectedOption = OUTCOME_TO_MESSAGE_ID_MAP.entries.find {
                it.key == reportFilterDropDownValue
            }
            valueString = systemImpl.getString(
                    selectedOption?.component2() ?: MR.strings.unset)
        }
        ReportFilter.FIELD_CONTENT_COMPLETION == reportFilterField -> {
            val selectedOption = ReportFilterEditPresenter.ContentCompletionStatusOption.values()
                    .find { it.optionVal == reportFilterDropDownValue }
            valueString = systemImpl.getString(
                    selectedOption?.stringResource ?: MR.strings.unset)
        }
        reportFilterCondition == ReportFilter.CONDITION_BETWEEN -> {
            valueString = """$reportFilterValueBetweenX ${systemImpl.getString(MR.strings.and_key)} $reportFilterValueBetweenY"""
        }
        reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY || reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON -> {
           valueString = "..."
        }
    }

    return "$fieldValue $conditionValue $valueString"
}