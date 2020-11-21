package com.ustadmobile.port.android.view.binding

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
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
    hint = if (customField != null) {
        UstadMobileSystemImpl.instance.getString(customField.customFieldLabelMessageID, context)
    } else {
        ""
    }
}

@BindingAdapter(value = ["textBitmaskValue", "textBitmaskFlags"], requireAll = false)
fun TextView.setBitmaskListText(textBitmaskValue: Long?, textBitmaskFlags: List<BitmaskFlag>?) {
    val systemImpl = UstadMobileSystemImpl.instance
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
    if (selectedOption != null) {
        text = if (selectedOption.customFieldValueOptionMessageId != 0) {
            UstadMobileSystemImpl.instance.getString(selectedOption.customFieldValueOptionMessageId, context)
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
    text = "${if (textFromDateLong > 0) dateFormat.format(textFromDateLong) else ""} - ${if (textToDateLong > 0) dateFormat.format(textToDateLong) else ""}"
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

@BindingAdapter(value = ["inventoryDescriptionStockCount", "inventoryDescriptionWeNames"])
fun TextView.setInventoryDescription(stockCount: Int, weNames: String){
    text = context.getString(R.string.x_item_by_y, stockCount.toString(), weNames)
}

@BindingAdapter("inventoryType")
fun TextView.getInventoryType(saleUid: Long){
    if(saleUid == 0L){
        text = context.getString(R.string.receive)
    }else{
        text = context.getString(R.string.sell)
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
fun TextView.setClazzWorkMarking(clazzMemberWithClazzWorkAndProgress: ClazzMemberWithClazzWorkProgress){
    var line = clazzMemberWithClazzWorkAndProgress.mClazzWorkSubmission.statusString(context)
    if(clazzMemberWithClazzWorkAndProgress.clazzWorkHasContent && clazzMemberWithClazzWorkAndProgress.mProgress >= 0) {
        line += " ${context.getString(R.string.completed)} " +
                "${clazzMemberWithClazzWorkAndProgress.mProgress.toInt()}% " +
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
fun TextView.setMemberRoleName(clazzMember: ClazzMember?) {
    text = clazzMember?.roleToString(context, UstadMobileSystemImpl.instance) ?: ""
}


@BindingAdapter("saleItemTotal")
fun TextView.setSaleItemTotal(saleItem: SaleItem?) {
    val amount: Float = (saleItem?.saleItemQuantity?:0) * (saleItem?.saleItemPricePerPiece ?: 0F)

    text = "" + amount.toString() +
            saleItem?.saleItemCurrency
}

@BindingAdapter("productNameWithCount")
fun TextView.setProductNameWithDeliveryCount(saleItemWithProduct: SaleItemWithProduct?){
    text = "" + saleItemWithProduct?.saleItemProduct?.productName + " (" +
            saleItemWithProduct?.deliveredCount + " " + context.getString(R.string.in_stock) + " )"
}

@BindingAdapter("inStockAppend")
fun TextView.setInStockAppend(stock: Int?){
    text = stock.toString() + " " + context.getString(R.string.in_stock)
}

@BindingAdapter(value = ["totalSale", "saleForTotalAfterDiscount"])
fun TextView.setSaleItemAfterDiscountTotal(totalSale : Long, sale: Sale?) {
    text = "" + (totalSale - (sale?.saleDiscount ?: 0L)) + " " + context.getString(R.string.afs)
}

@BindingAdapter("weTotalSaleValue")
fun TextView.setWeTotalSale(personWithSaleInfo: PersonWithSaleInfo?){
    text = personWithSaleInfo?.totalSale.toString() + " " + context.getString(R.string.afs) +
            " " +  context.getString(R.string.total_sales)
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