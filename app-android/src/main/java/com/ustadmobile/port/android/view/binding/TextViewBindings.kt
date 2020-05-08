package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValue
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField

@BindingAdapter("textMessageId")
fun TextView.setTextMessageId(messageId: Int) {
    text = UstadMobileSystemImpl.instance.getString(messageId, context)
}

@BindingAdapter("hintMessageId")
fun TextView.setHintMessageId(messageId: Int) {
    hint = UstadMobileSystemImpl.instance.getString(messageId, context)
}

@BindingAdapter("customFieldHint")
fun TextView.setCustomFieldHint(customField: CustomField?) {
    hint = if(customField != null) {
        UstadMobileSystemImpl.instance.getString(customField.customFieldLabelMessageID, context)
    }else {
        ""
    }
}

@BindingAdapter(value = ["textBitmaskValue", "textBitmaskFlags"], requireAll = false)
fun TextView.setBitmaskListText(textBitmaskValue: Long?, textBitmaskFlags: List<BitmaskFlag>?) {
    val systemImpl = UstadMobileSystemImpl.instance
    if(textBitmaskValue == null || textBitmaskFlags == null)
        return

    text = textBitmaskFlags.filter { (it.flagVal and textBitmaskValue) == it.flagVal }
            .joinToString { systemImpl.getString(it.messageId, context) }
}

@BindingAdapter("presenterFieldHeader")
fun TextView.setPresenterFieldHeader(presenterField: PersonDetailPresenterField) {
    if(presenterField.headerMessageId != 0) {
        text = UstadMobileSystemImpl.instance.getString(presenterField.headerMessageId, context)
    }
}

@BindingAdapter(value= ["textMessageIdOptionSelected","textMessageIdOptions"], requireAll = true)
fun TextView.setTextFromMessageIdList(textMessageIdOptionSelected: Int, textMessageIdOptions: List<MessageIdOption>) {
    text = UstadMobileSystemImpl.instance.getString(textMessageIdOptions
            ?.firstOrNull { it.code == textMessageIdOptionSelected }?.messageId ?: 0, context)
}

@BindingAdapter(value = ["textCustomFieldValue", "textCustomFieldValueOptions"])
fun TextView.setTextFromCustomFieldDropDownOption(customFieldValue: CustomFieldValue?,
                                                  customFieldValueOptions: List<CustomFieldValueOption>?) {
    val selectedOption = customFieldValueOptions
            ?.firstOrNull { it.customFieldValueOptionUid == customFieldValue?.customFieldValueCustomFieldValueOptionUid }
    if(selectedOption != null) {
        text = if(selectedOption.customFieldValueOptionMessageId != 0) {
            UstadMobileSystemImpl.instance.getString(selectedOption.customFieldValueOptionMessageId, context)
        }else {
            selectedOption.customFieldValueOptionName ?: ""
        }
    }else {
        text = ""
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["textFromDateLong", "textToDateLong"])
fun TextView.setTextFromToDateLong(textFromDateLong: Long, textToDateLong: Long) {
    val dateFormat = DateFormat.getDateFormat(context)
    text = "${if(textFromDateLong > 0) dateFormat.format(textFromDateLong) else ""} - ${if(textToDateLong > 0) dateFormat.format(textToDateLong) else ""}"
}

