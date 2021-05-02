package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.*
import java.util.*
import com.soywiz.klock.DateFormat as KlockDateFormat
import com.ustadmobile.core.util.ext.roleToString
import com.ustadmobile.core.util.ext.outcomeToString
import com.ustadmobile.core.util.ext.systemImpl
import java.util.concurrent.TimeUnit

@BindingAdapter("textMessageId")
fun TextView.setTextMessageId(messageId: Int) {
    text = systemImpl.getString(messageId, context)
}

@BindingAdapter("hintMessageId")
fun TextView.setHintMessageId(messageId: Int) {
    hint = systemImpl.getString(messageId, context)
}

@BindingAdapter("customFieldHint")
fun TextView.setCustomFieldHint(customField: CustomField?) {
    hint = if (customField != null) {
        systemImpl.getString(customField.customFieldLabelMessageID, context)
    } else {
        ""
    }
}

@BindingAdapter(value = ["textBitmaskValue", "textBitmaskFlags"], requireAll = false)
fun TextView.setBitmaskListText(textBitmaskValue: Long?, textBitmaskFlags: List<BitmaskFlag>?) {
    if (textBitmaskValue == null || textBitmaskFlags == null)
        return

    text = textBitmaskFlags.filter { (it.flagVal and textBitmaskValue) == it.flagVal }
            .joinToString { systemImpl.getString(it.messageId, context) }
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
        fallbackMessageId?.let { systemImpl.getString(it, context) } ?: "")

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
            text = systemImpl.getString(messageId, context)
        }else if(fallbackMessage != null) {
            text = fallbackMessage
        }
    }
}

@BindingAdapter(value= ["textMessageIdOptionSelected","textMessageIdOptions"], requireAll = true)
fun TextView.setTextFromMessageIdList(textMessageIdOptionSelected: Int, textMessageIdOptions: List<MessageIdOption>) {
    text = systemImpl.getString(textMessageIdOptions
            ?.firstOrNull { it.code == textMessageIdOptionSelected }?.messageId ?: 0, context)
}

@BindingAdapter(value = ["textCustomFieldValue", "textCustomFieldValueOptions"])
fun TextView.setTextFromCustomFieldDropDownOption(customFieldValue: CustomFieldValue?,
                                                  customFieldValueOptions: List<CustomFieldValueOption>?) {
    val selectedOption = customFieldValueOptions
            ?.firstOrNull { it.customFieldValueOptionUid == customFieldValue?.customFieldValueCustomFieldValueOptionUid }
    if (selectedOption != null) {
        text = if (selectedOption.customFieldValueOptionMessageId != 0) {
            systemImpl.getString(selectedOption.customFieldValueOptionMessageId, context)
        } else {
            selectedOption.customFieldValueOptionName ?: ""
        }
    } else {
        text = ""
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["textFromDateLong", "textToDateLong"])
fun TextView.setTextFromToDateLong(textFromDateLong: Long, textToDateLong: Long) {
    val dateFormat = DateFormat.getDateFormat(context)
    text = "${if (textFromDateLong > 0) dateFormat.format(textFromDateLong) else ""} -" +
            " ${if (textToDateLong > 0 && textToDateLong != Long.MAX_VALUE) dateFormat.format(textToDateLong) else ""}"
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["enrolmentTextFromDateLong", "enrolmentTextToDateLong"])
fun TextView.setEnrolmentTextFromToDateLong(textFromDateLong: Long, textToDateLong: Long) {
    val dateFormat = DateFormat.getDateFormat(context)
    text = "${if (textFromDateLong > 0) dateFormat.format(textFromDateLong) else ""} -" +
            " ${if (textToDateLong > 0 && textToDateLong != Long.MAX_VALUE) dateFormat.format(textToDateLong) else context.getString(R.string.time_present)}"
}



private val textViewSchoolGenderStringIds: Map<Int, Int> = mapOf(
        School.SCHOOL_GENDER_MIXED to R.string.mixed,
        School.SCHOOL_GENDER_FEMALE to R.string.female,
        School.SCHOOL_GENDER_MALE to R.string.male
)


@BindingAdapter("textSchoolGender")
fun TextView.setSchoolGenderText(gender: Int) {
    val genderStringId = textViewSchoolGenderStringIds[gender]
    text = if (genderStringId != null) {
        context.getString(genderStringId)
    } else {
        ""
    }
}

@BindingAdapter("textClazzLogStatus")
fun TextView.setTextClazzLogStatus(clazzLog: ClazzLog) {
    text = when (clazzLog.clazzLogStatusFlag) {
        ClazzLog.STATUS_CREATED -> context.getString(R.string.not_recorded)
        ClazzLog.STATUS_HOLIDAY -> "${context.getString(R.string.holiday)} - ${clazzLog.cancellationNote}"
        ClazzLog.STATUS_RECORDED -> context.getString(R.string.present_late_absent,
                clazzLog.clazzLogNumPresent, clazzLog.clazzLogNumPartial, clazzLog.clazzLogNumAbsent)
        else -> ""
    }
}

private val klockDateFormat: KlockDateFormat by lazy { KlockDateFormat("EEE") }

@BindingAdapter("textShortDayOfWeek")
fun TextView.setTextShortDayOfWeek(localTime: DateTimeTz) {
    text = klockDateFormat.format(localTime)
}

@SuppressLint("SetTextI18n")
@BindingAdapter(value = ["textLocalDateTime", "textLocalDateTimeZone"])
fun TextView.setTextLocalDayAndTime(time: Long, timeZone: TimeZone) {
    val dateFormat = DateFormat.getMediumDateFormat(context)
    val timeFormat = DateFormat.getTimeFormat(context)
    timeFormat.timeZone = timeZone
    dateFormat.timeZone = timeZone
    text = dateFormat.format(time) + " - " + timeFormat.format(time)
}

@BindingAdapter("textDate")
fun TextView.setDateText(time: Long){
    val dateFormat = DateFormat.getDateFormat(context)
    text = if (time > 0) dateFormat.format(time) else ""
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
fun TextView.setClazzWorkMarking(clazzEnrolmentWithClazzWorkAndProgress: ClazzEnrolmentWithClazzWorkProgress){
    var line = clazzEnrolmentWithClazzWorkAndProgress.mClazzWorkSubmission.statusString(context)
    if(clazzEnrolmentWithClazzWorkAndProgress.clazzWorkHasContent && clazzEnrolmentWithClazzWorkAndProgress.mProgress >= 0) {
        line += " ${context.getString(R.string.completed)} " +
                "${clazzEnrolmentWithClazzWorkAndProgress.mProgress.toInt()}% " +
                context.getString(R.string.of_content)
    }
    text = line
}

fun ClazzWorkSubmission?.statusString(context: Context) = when {
    this == null -> context.getString(R.string.not_submitted_cap)
    this.clazzWorkSubmissionDateTimeMarked > 0 -> context.getString(R.string.marked).capitalize()
    this.clazzWorkSubmissionDateTimeFinished > 0 -> context.getString(R.string.submitted).capitalize()
    else -> context.getString(R.string.not_submitted_cap).capitalize()

}

@BindingAdapter(value=["selectedClazzWorkQuestionType"])
fun TextView.setTypeText(clazzWorkQuestionType: Int){
    if(clazzWorkQuestionType == ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT){
        text = context.getString(R.string.sel_question_type_free_text)
    }else if(clazzWorkQuestionType == ClazzWorkQuestion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE){
        text = context.getString(R.string.quiz)
    }
}


@BindingAdapter(value=["responseTextFilled"])
fun TextView.setResponseTextFilled(responseText: String?){
    if(responseText == null || responseText.isEmpty()){
        text = context.getString(R.string.not_answered)
    }else{
        text = responseText
    }
}

@BindingAdapter("memberRoleName")
fun TextView.setMemberRoleName(clazzEnrolment: ClazzEnrolment?) {
    text = clazzEnrolment?.roleToString(context, systemImpl) ?: ""
}

@BindingAdapter("memberEnrolmentOutcomeWithReason")
fun TextView.setMemberEnrolmentOutcome(clazzEnrolment: ClazzEnrolmentWithLeavingReason?){
    text = "${clazzEnrolment?.roleToString(context, systemImpl)} - ${clazzEnrolment?.outcomeToString(context,  systemImpl)}"
}

@BindingAdapter("clazzEnrolmentWithClazzAndOutcome")
fun TextView.setClazzEnrolmentWithClazzAndOutcome(clazzEnrolment: ClazzEnrolmentWithClazz?){
    text = "${clazzEnrolment?.clazz?.clazzName} (${clazzEnrolment?.roleToString(context, systemImpl)}) - ${clazzEnrolment?.outcomeToString(context,  systemImpl)}"
}

@BindingAdapter("showisolang")
fun TextView.setIsoLang(language: Language){
    var isoText = ""
    if(language.iso_639_1_standard?.isNotEmpty() == true){
        isoText += language.iso_639_1_standard
    }
    if(language.iso_639_2_standard?.isNotEmpty() == true){
        isoText += "/${language.iso_639_2_standard}"
    }
    text = isoText
}

@BindingAdapter("rolesAndPermissionsText")
fun TextView.setRolesAndPermissionsText(entityRole: EntityRoleWithNameAndRole){
    val scopeType = when (entityRole.erTableId) {
        School.TABLE_ID -> {
            " (" +context.getString(R.string.school)+ ")"
        }
        Clazz.TABLE_ID -> {
            " (" +context.getString(R.string.clazz) + ")"
        }
        Person.TABLE_ID -> {
            " (" + context.getString(R.string.person) + ")"
        }
        else -> ""
    }

    val fullText =entityRole.entityRoleRole?.roleName +  " @ " +
            entityRole.entityRoleScopeName + scopeType
    text = fullText

}

@BindingAdapter("statementDate")
fun TextView.setStatementDate(person: PersonWithAttemptsSummary){
    val dateFormatter = DateFormat.getDateFormat(context)
    var statementDate = dateFormatter.format(person.startDate)

    if(person.endDate != 0L && person.endDate != Long.MAX_VALUE){
        val startDate = DateTime(person.startDate)
        val endDate = DateTime(person.endDate)
        if(startDate.dayOfYear != endDate.dayOfYear){
            statementDate += " - ${dateFormatter.format(person.endDate)}"
        }
    }

    text = statementDate

}

@BindingAdapter("shortDateTime")
fun TextView.setShortDateTime(time: Long){
    val dateFormat = DateFormat.getDateFormat(context)
    val timeFormat = DateFormat.getTimeFormat(context)
    text = dateFormat.format(time) + " - " + timeFormat.format(time)
}

@BindingAdapter("durationHoursMins")
fun TextView.setDurationHoursAndMinutes(duration: Long){
    val hours = TimeUnit.MILLISECONDS.toHours(duration).toInt()

    var minutes = TimeUnit.MILLISECONDS.toMinutes(duration)

    var durationString = " "

    if(hours >= 1){
        minutes -= TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration))
        durationString += "${resources.getQuantityString(R.plurals.duration_hours, hours, hours)} "
    }

    durationString += resources.getQuantityString(R.plurals.duration_minutes,
            minutes.toInt(), minutes.toInt())

    text = durationString

}

@BindingAdapter("durationMinsSecs")
fun TextView.setDurationMinutesAndSeconds(duration: Long){
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()

    var seconds = TimeUnit.MILLISECONDS.toSeconds(duration)

    var durationString = " "

    if(minutes >= 1){
        seconds -= TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        durationString += "${resources.getQuantityString(R.plurals.duration_minutes, minutes, minutes)} "
    }

    durationString += resources.getQuantityString(R.plurals.duration_seconds,
            seconds.toInt(), seconds.toInt())

    text = durationString

}


@BindingAdapter("isContentComplete")
fun TextView.setContentComplete(person: PersonWithSessionsDisplay){
    text = if(person.resultComplete){
        when(person.resultSuccess){
            StatementEntity.RESULT_SUCCESS -> {
                context.getString(R.string.passed)
            }
            StatementEntity.RESULT_FAILURE -> {
                context.getString(R.string.failed)
            }
            StatementEntity.RESULT_UNSET ->{
                context.getString(R.string.completed)
            }else ->{
                ""
            }
        }
    }else{
        context.getString(R.string.incomplete)
    } + " - "
}