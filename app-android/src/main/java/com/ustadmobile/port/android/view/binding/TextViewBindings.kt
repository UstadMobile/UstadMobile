package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.soywiz.klock.DateTimeTz
import com.toughra.ustadmobile.R
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.model.BitmaskMessageId
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.VerbEntity.Companion.VERB_ANSWERED_UID
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.datetime.TimeZone as KxTimeZone
import com.ustadmobile.core.R as CR

@BindingAdapter("chatMessage", "loggedInPersonUid")
fun TextView.setChatMessageTitle(message: MessageWithPerson, loggedInPersonUid: Long){
    if(message.messagePerson?.personUid == loggedInPersonUid){
        text = context.getString(CR.string.you)
        if(message.messageTableId == Chat.TABLE_ID){
            gravity = Gravity.END
        }
    }else{
        text = (message.messagePerson?.fullName()?:"") + " "
        if(message.messageTableId == Chat.TABLE_ID) {
            gravity = Gravity.START
        }
    }
}

@BindingAdapter("chatMessageOrientation", "loggedInPersonUidOrientation")
fun TextView.setChatMessagOrientation(message: MessageWithPerson, loggedInPersonUid: Long){
    if(message.messageTableId == Chat.TABLE_ID) {
        gravity = if (message.messagePerson?.personUid == loggedInPersonUid) {
            Gravity.END
        } else {
            Gravity.START
        }
    }

    if(message.messageRead == null && message.messagePerson?.personUid != loggedInPersonUid){
        setTypeface(typeface, Typeface.BOLD)
    }else{
        setTypeface(typeface, Typeface.NORMAL)
    }
}

@BindingAdapter(value = ["textBitmaskValue", "textBitmaskFlags"], requireAll = false)
fun TextView.setBitmaskListText(textBitmaskValue: Long?, textBitmaskFlags: List<BitmaskFlag>?) {
    if (textBitmaskValue == null || textBitmaskFlags == null)
        return

    text = textBitmaskFlags.filter { (it.flagVal and textBitmaskValue) == it.flagVal }
            .joinToString { systemImpl.getString(it.stringResource) }
}

@BindingAdapter(value = ["bitmaskValue", "flagMessageIds"], requireAll = false)
fun TextView.setBitmaskListTextFromMap(bitmaskValue: Long?, flagMessageIds: List<BitmaskMessageId>?) {
    if(bitmaskValue == null || flagMessageIds == null)
        return

    val impl = systemImpl

    text = flagMessageIds.map { it.toBitmaskFlag(bitmaskValue) }
        .filter { it.enabled }
        .joinToString { impl.getString(it.stringResource) }
}




private val textViewSchoolGenderStringIds: Map<Int, Int> = mapOf(
        School.SCHOOL_GENDER_MIXED to CR.string.mixed,
        School.SCHOOL_GENDER_FEMALE to CR.string.female,
        School.SCHOOL_GENDER_MALE to CR.string.male
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


@BindingAdapter("textShortDayOfWeek")
@Deprecated("This will be removed after switching to mvvm")
fun TextView.setTextShortDayOfWeek(localTime: DateTimeTz) {
    //do nothing
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


@BindingAdapter("chipMemberRoleName")
fun Chip.setChipMemberRoleName(clazzEnrolment: ClazzEnrolment?) {
    text = clazzEnrolment?.roleToString(context, systemImpl) ?: ""
}


@BindingAdapter("memberRoleName")
fun TextView.setMemberRoleName(clazzEnrolment: ClazzEnrolment?) {
    text = clazzEnrolment?.roleToString(context, systemImpl) ?: ""
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

@BindingAdapter(value=["statementStartDate", "statementEndDate"])
fun TextView.setStatementDate(statementStartDate: Long, statementEndDate: Long){
    if(statementStartDate == 0L){
        text = ""
        return
    }

    val dateFormatter = DateFormat.getDateFormat(context)
    var statementDate = dateFormatter.format(statementStartDate)

    if(statementEndDate != 0L && statementEndDate!= Long.MAX_VALUE){
        val startDate = Instant.fromEpochMilliseconds(statementStartDate)
            .toLocalDateTime(KxTimeZone.currentSystemDefault())
        val endDate = Instant.fromEpochMilliseconds(statementEndDate)
            .toLocalDateTime(KxTimeZone.currentSystemDefault())
        if(startDate.dayOfYear != endDate.dayOfYear) {
            statementDate += " - ${dateFormatter.format(statementEndDate)}"
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
        durationString += "${resources.getQuantityString(CR.plurals.duration_hours, hours, hours)} "
    }

    durationString += resources.getQuantityString(CR.plurals.duration_minutes,
            minutes.toInt(), minutes.toInt())

    text = durationString

}

@BindingAdapter("scorePercentage")
fun TextView.setScorePercentage(scoreProgress: ContentEntryStatementScoreProgress?){
    if(scoreProgress == null){
        return
    }
    // (4/5) * (1 - 20%) = penalty applied to score
    text = "${scoreProgress.calculateScoreWithPenalty()}%"
}

@BindingAdapter("scoreWithWeight")
fun TextView.setScoreWithWeight(scoreProgress: ContentEntryStatementScoreProgress?){
    if(scoreProgress == null){
        return
    }
    text = "${scoreProgress.calculateScoreWithWeight()}%"
}



@BindingAdapter("durationMinsSecs")
fun TextView.setDurationMinutesAndSeconds(duration: Long){
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()

    var seconds = TimeUnit.MILLISECONDS.toSeconds(duration)

    var durationString = " "

    if(minutes >= 1){
        seconds -= TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        durationString += "${resources.getQuantityString(CR.plurals.duration_minutes, minutes, minutes)} "
    }

    durationString += resources.getQuantityString(CR.plurals.duration_seconds,
            seconds.toInt(), seconds.toInt())

    text = durationString

}

@BindingAdapter("statementQuestionAnswer")
fun TextView.setStatementQuestionAnswer(statementEntity: StatementEntity){
    if(statementEntity.statementVerbUid != VERB_ANSWERED_UID){
        visibility = View.GONE
        return
    }else{
        visibility = View.VISIBLE
    }
    val fullStatementJson = statementEntity.fullStatement ?: return
    val statement = gson.fromJson(fullStatementJson, Statement::class.java)
    var statementText = statement?.`object`?.definition?.description?.get("en-US")
    val answerResponse = statement.result?.response
    if(answerResponse?.isNotEmpty() == true || answerResponse?.contains("[,]") == true){
        val responses = answerResponse.split("[,]")
        val choiceMap = statement.`object`?.definition?.choices
        val sourceMap = statement?.`object`?.definition?.source
        val targetMap = statement?.`object`?.definition?.target
        statementText += "<br />"
        responses.forEachIndexed { i, it ->

            var description = choiceMap?.find { choice -> choice.id == it }?.description?.get("en-US")
            if(it.contains("[.]")){
                val dragResponse = it.split("[.]")
                description = ""
                description += sourceMap?.find { source -> source.id == dragResponse[0] }?.description?.get("en-US")
                description += " on "
                description += targetMap?.find { target -> target.id == dragResponse[1] }?.description?.get("en-US")
            }


            statementText += "${i+1}: ${if(description.isNullOrEmpty()) it else description} <br />"


        }

    }
    text = HtmlCompat.fromHtml(statementText ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)


}


@BindingAdapter("isContentComplete")
fun TextView.setContentComplete(person: PersonWithSessionsDisplay){
    text = if(person.resultComplete){
        when(person.resultSuccess){
            StatementEntity.RESULT_SUCCESS -> {
                context.getString(CR.string.passed)
            }
            StatementEntity.RESULT_FAILURE -> {
                context.getString(CR.string.failed)
            }
            StatementEntity.RESULT_UNSET ->{
                context.getString(CR.string.completed)
            }else ->{
                ""
            }
        }
    }else{
        context.getString(CR.string.incomplete)
    } + " - "
}