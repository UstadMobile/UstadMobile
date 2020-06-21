package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.soywiz.klock.DateTimeTz
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.lib.db.entities.*
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.UMFileUtil
import java.util.*
import com.soywiz.klock.DateFormat as KlockDateFormat

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

/**
 * This binder will handle situations where a there is a fixed list of flags, each of which
 * corresponds to a given messageId.
 *
 * e.g.
 *
 * class MyPresenter {
 *    companion object {
 *       @JvmField
 *       val ROLE_MAP = mapOf(ClazzMember.ROLE_STUDENT to MessageID.student,
 *                       ClazzMember.ROLE_TEACHER to MessageID.teacher)
 *    }
 * }
 *
 * You can then use the following in the view XML:
 *
 * &lt;import class="com.packagepath.MyPresenter"/&gt;
 *
 * &lt;TextView
 * ...
 * app:textMessageIdLookupKey="@{entityObject.memberRole}"
 * app:textMessageIdLookupMap="@{MyPresenter.ROLE_MAP}"
 * /&gt;
 *
 * Note textMessageIdLookupKey and textMessageIdLookupMap are in separate binders because if they
 * are in the same binder the generated data binding does not always update it when one is set
 * after the other.
 */
@BindingAdapter("textMessageIdLookupKey")
fun TextView.setTextMessageIdOptionSelected(textMessageIdLookupKey: Int) {
    setTag(R.id.tag_messageidoption_selected, textMessageIdLookupKey)
    updateFromTextMessageIdOptions()
}

@BindingAdapter(value = ["textMessageIdLookupMap", "fallbackMessageId", "fallbackMessage"], requireAll = false)
fun TextView.setTextMessageIdOptions(textMessageIdLookupMap: Map<Int, Int>?,
                                     fallbackMessageId: Int?, fallbackMessage: String?) {
    setTag(R.id.tag_messageidoptions_list, textMessageIdLookupMap)
    setTag(R.id.tag_messageidoption_fallback, fallbackMessage ?:
        fallbackMessageId?.let { UstadMobileSystemImpl.instance.getString(it, context) } ?: "")

    updateFromTextMessageIdOptions()
}

@SuppressLint("SetTextI18n")
private fun TextView.updateFromTextMessageIdOptions() {
    val currentOption = getTag(R.id.tag_messageidoption_selected) as? Int
    val textMessageIdOptions = getTag(R.id.tag_messageidoptions_list) as? Map<Int, Int>
    val fallbackMessage = getTag(R.id.tag_messageidoption_fallback) as? String
    if(currentOption != null && textMessageIdOptions != null) {
        val messageId = textMessageIdOptions[currentOption]
        if(messageId != null) {
            text = UstadMobileSystemImpl.instance.getString(messageId, context)
        }else if(fallbackMessage != null) {
            text = fallbackMessage
        }
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


private val textViewSchoolGenderStringIds: Map<Int, Int> = mapOf(
        School.SCHOOL_GENDER_MIXED  to R.string.mixed,
        School.SCHOOL_GENDER_FEMALE to R.string.female,
        School.SCHOOL_GENDER_MALE to R.string.male
)


@BindingAdapter("textSchoolGender")
fun TextView.setSchoolGenderText(gender: Int) {
    val genderStringId = textViewSchoolGenderStringIds[gender]
    text = if(genderStringId != null) {
        context.getString(genderStringId)
    }else {
        ""
    }
}

@BindingAdapter("textClazzLogStatus")
fun TextView.setTextClazzLogStatus(clazzLog: ClazzLog) {
    text = when(clazzLog.clazzLogStatusFlag) {
        ClazzLog.STATUS_CREATED -> context.getString(R.string.not_recorded)
        ClazzLog.STATUS_HOLIDAY -> "${context.getString(R.string.holiday)} - ${clazzLog.cancellationNote}"
        ClazzLog.STATUS_RECORDED -> context.getString(R.string.present_late_absent,
                clazzLog.clazzLogNumPresent, clazzLog.clazzLogNumPartial, clazzLog.clazzLogNumAbsent)
        else -> ""
    }
}

private val klockDateFormat :KlockDateFormat by lazy { KlockDateFormat("EEE") }

@BindingAdapter("textShortDayOfWeek")
fun TextView.setTextShortDayOfWeek(localTime: DateTimeTz) {
    text = klockDateFormat.format(localTime)
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value=["textLocalDateTime", "textLocalDateTimeZone"])
fun TextView.setTextLocalDayAndTime(time: Long, timeZone: TimeZone){
    val dateFormat = DateFormat.getMediumDateFormat(context)
    val timeFormat = DateFormat.getTimeFormat(context)
    timeFormat.timeZone = timeZone
    dateFormat.timeZone = timeZone
    text = dateFormat.format(time) + " - " + timeFormat.format(time)
}

@BindingAdapter("htmlText")
fun TextView.setHtmlText(htmlText: String?) {
    text = if(htmlText != null) HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY) else ""
}

@BindingAdapter("fileSize")
fun TextView.setFileSize(fileSize: Long) {
    text = UMFileUtil.formatFileSize(fileSize)
}

@BindingAdapter(value=["clazzMemberWithClazzWorkAndProgress"])
fun TextView.setClazzWorkMarking(clazzMemberWithClazzWorkAndProgress: ClazzMemberWithClazzWorkProgress){
    val marked = context.getString(R.string.marked_cap)
    val completed = context.getString(R.string.completed)
    val submitted = context.getString(R.string.submitted_cap)
    val notSubmitted = context.getString(R.string.not_submitted_cap)
    val ofContent = context.getString(R.string.of_content)

    val clazzWorkSubmission = clazzMemberWithClazzWorkAndProgress.mClazzWorkSubmission
    var line2Bit1 = ""
    var line2Bit2 = ""
    if(clazzWorkSubmission == null){
        line2Bit1 = notSubmitted
    }else{
        line2Bit1 = if(clazzWorkSubmission.clazzWorkSubmissionDateTimeFinished < 1){
            submitted
        }else{
            marked
        }
    }

    if(clazzMemberWithClazzWorkAndProgress.mProgress >= 0){
        line2Bit2 = " - " + completed + " " + clazzMemberWithClazzWorkAndProgress.mProgress +
                "% " + ofContent
    }
    val line2 = line2Bit1 + line2Bit2

    text = line2
}