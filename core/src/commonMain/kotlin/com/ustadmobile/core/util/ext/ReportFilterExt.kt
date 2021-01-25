package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ReportFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

fun ReportFilter.toDisplayString(context: Any, db: UmAppDatabase): String {
    val fieldOption = ReportFilterEditPresenter.FieldOption.values()
            .find { it.optionVal == reportFilterField } ?: return ""
    val conditionOption = ReportFilterEditPresenter.ConditionOption.values()
            .find { it.optionVal == reportFilterCondition } ?: return ""

    val fieldValue = UstadMobileSystemImpl.instance.getString(fieldOption.messageId, context)
    val conditionValue = UstadMobileSystemImpl.instance.getString(conditionOption.messageId, context)

    var valueString = reportFilterValue
    when {
        ReportFilter.FIELD_PERSON_GENDER == reportFilterField -> {

            val selectedOption = ReportFilterEditPresenter.genderMap.entries.find {
                it.key == reportFilterDropDownValue
            }
            valueString = UstadMobileSystemImpl.instance.getString(
                    selectedOption?.component2() ?: 0, context)
        }
        ReportFilter.FIELD_CONTENT_COMPLETION == reportFilterField -> {
            val selectedOption = ReportFilterEditPresenter.ContentCompletionStatusOption.values()
                    .find { it.optionVal == reportFilterDropDownValue }
            valueString = UstadMobileSystemImpl.instance.getString(
                    selectedOption?.messageId ?: 0, context)
        }
        reportFilterCondition == ReportFilter.CONDITION_BETWEEN -> {
            valueString = """$reportFilterValueBetweenX and $reportFilterValueBetweenY"""
        }
        reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY -> {
            val list = valueString?.split(", ")?.map { it.toLong() } ?: listOf()
            GlobalScope.async(Dispatchers.Main) {
                val labels = db.contentEntryDao.getContentEntryFromUids(list)
                valueString = labels.map { it.labelName }.joinToString()
                return@async
            }
        }
    }

    return "$fieldValue $conditionValue $valueString"
}