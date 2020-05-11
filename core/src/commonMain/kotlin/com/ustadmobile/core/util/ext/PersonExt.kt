package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.CustomField.Companion.FIELD_TYPE_DATE_SPINNER
import com.ustadmobile.lib.db.entities.CustomField.Companion.FIELD_TYPE_DROPDOWN
import com.ustadmobile.lib.db.entities.CustomField.Companion.FIELD_TYPE_TEXT
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ADDRESS
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_EMAIL
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_GENDER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_USERNAME
import kotlin.reflect.KMutableProperty0

class PersonPresenterFieldRowAdapter(val populateFn: (Person, PresenterFieldRow) -> Unit,
    val updateFn: (Person, PresenterFieldRow) -> Unit)

fun PresenterFieldRow.populateAsStringField(nameMessageId: Int, strValue: String?,
                                            inputType: Int = CustomField.INPUT_TYPE_TEXT,
                                            iconId: Int = 0, actionOnClick: String? = null) {
    customField = CustomField(customFieldLabelMessageID = nameMessageId,
        customFieldType = FIELD_TYPE_TEXT, customFieldInputType = inputType,
        customFieldIconId =  iconId, actionOnClick = actionOnClick)
    customFieldValue = CustomFieldValue(customFieldValueValue = strValue)
}

fun PresenterFieldRow.populateAsDateField(nameMessageId: Int, dateValue: Long,
        iconId: Int = CustomField.ICON_CALENDAR) {
    customField = CustomField(customFieldLabelMessageID = nameMessageId,
        customFieldType = FIELD_TYPE_DATE_SPINNER, customFieldIconId = iconId)
    customFieldValue = CustomFieldValue(customFieldValueCustomFieldValueOptionUid = dateValue)
}

fun PresenterFieldRow.populateAsDropdown(nameMessageId: Int, currentValue: Int,
                                         optionsList: List<Pair<Int, Int>>, iconId: Int = 0) {
    customField = CustomField(customFieldLabelMessageID = nameMessageId,
        customFieldType = FIELD_TYPE_DROPDOWN, customFieldIconId = iconId)
    customFieldOptions = optionsList.map { CustomFieldValueOption().apply {
        customFieldValueOptionMessageId =it.first
        customFieldValueOptionUid = it.second.toLong()
    } }
    customFieldValue = CustomFieldValue(customFieldValueCustomFieldValueOptionUid = currentValue.toLong())
}


fun Person.updateStringFieldFromRow(property: KMutableProperty0<String?>, row: PresenterFieldRow?) {
    property.set(row?.customFieldValue?.customFieldValueValue)
    //setter(row?.customFieldValue?.customFieldValueValue)
}

fun Person.updateDateFieldFromRow(property:KMutableProperty0<Long>, row: PresenterFieldRow) {
    property.set(row.customFieldValue?.customFieldValueCustomFieldValueOptionUid ?: 0L)
}

fun Person.updateIntFromDropDown(property: KMutableProperty0<Int>, row: PresenterFieldRow) {
    property.set(row.customFieldValue?.customFieldValueCustomFieldValueOptionUid?.toInt() ?: 0)
}

/**
 * Get the CustomField and CustomFieldValue for a given field uid
 * @param fieldUid as per PERSON_FIELD_UID constants on PersonDetailPresenterField
 */
fun Person.populatePresenterFieldRow(presenterFieldRow: PresenterFieldRow): Boolean {
    val fieldUid = presenterFieldRow.presenterField?.fieldUid?.toInt() ?: return false
    val adapter = ADAPTER_MAP[fieldUid] ?: return false
    adapter.populateFn(this, presenterFieldRow)
    return true
}

/**
 * Given a list of PresenterFieldRow defining the rows that should be displayed (e.g. for view or
 * editing), this will fill in the CustomField and CustomFieldValue object for values that are stored
 * directly on the Person entity itself
 *
 * @param presenterFields fields to be displayed
 * @return List of PresenterFieldRow where the placeholders for fields that come from the Person
 * entity itself are filled in with the values from this Person instance
 */
fun Person.populatePresenterFields(presenterFields: List<PresenterFieldRow>): List<PresenterFieldRow> {
    presenterFields.forEach {
        if(it.customField == null
                && it.presenterField?.fieldType == PersonDetailPresenterField.TYPE_FIELD) {
            populatePresenterFieldRow(it)
        }
    }

    return presenterFields
}

/**
 * Given a list of PresenterFieldRow this will set the fields on the person object (e.g. for use
 * after editing has been completed)
 * @param presenterFields a list of PresenterFieldRow containing the values from which this Person
 * instance should be updated.
 */
fun Person.updateFromFieldList(presenterFields: List<PresenterFieldRow>) {
    presenterFields.filter { it.presenterField?.isCoreEntityField() ?: false}.forEach {
        val presenterFieldUid = it.presenterField?.fieldUid ?: return@forEach
        val adapter = ADAPTER_MAP[presenterFieldUid.toInt()] ?: return@forEach
        adapter.updateFn(this, it)
    }
}

//Adapter that might work...
data class PersonCustomField(val customField: CustomField, val loader: (Person) -> Pair<CustomField, List<CustomFieldValueOption>>,
                             val saver: (fieldRow: PresenterFieldRow, person: Person) -> Unit)


private val personGenderCustomFieldValueOptions = listOf(
    CustomFieldValueOption().apply {
        customFieldValueOptionName = "Male"
        customFieldValueOptionUid = Person.GENDER_MALE.toLong()
    },
    CustomFieldValueOption().apply {
        customFieldValueOptionName = "Female"
        customFieldValueOptionUid = Person.GENDER_FEMALE.toLong()
    }
)

val ADAPTER_MAP = mapOf(
        PERSON_FIELD_UID_FIRST_NAMES to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsStringField(MessageID.first_names, person.firstNames,
                    iconId = CustomField.ICON_PERSON)},
                {person, row -> person.updateStringFieldFromRow(person::firstNames, row)}),
        PERSON_FIELD_UID_LAST_NAME to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsStringField(MessageID.last_name, person.lastName)},
                {person, row -> person.updateStringFieldFromRow(person::lastName, row)}),
        PERSON_FIELD_UID_BIRTHDAY to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsDateField(MessageID.birthday, person.dateOfBirth)},
                {person, row -> person.updateDateFieldFromRow(person::dateOfBirth, row)}),
        PERSON_FIELD_UID_ADDRESS to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsStringField(MessageID.address, person.personAddress,
                    iconId = CustomField.ICON_ADDRESS)},
                {person, row -> person.updateStringFieldFromRow(person::personAddress, row)}),
        PERSON_FIELD_UID_USERNAME to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsStringField(MessageID.username, person.username)},
                {person, row -> person.updateStringFieldFromRow(person::username, row)}),
        PERSON_FIELD_UID_PHONE_NUMBER to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsStringField(MessageID.phone_number, person.phoneNum,
                    inputType = CustomField.INPUT_TYPE_PHONENUM, iconId = CustomField.ICON_PHONE,
                    actionOnClick = CustomField.ACTION_CALL)},
                {person, row -> person.updateStringFieldFromRow(person::phoneNum, row)}),
        PERSON_FIELD_UID_GENDER to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsDropdown(MessageID.gender_literal, person.gender,
                    listOf(Pair(MessageID.male, Person.GENDER_MALE),
                            Pair(MessageID.female, Person.GENDER_FEMALE)))},
                {person, row -> person.updateIntFromDropDown(person::gender, row) }),
        PERSON_FIELD_UID_EMAIL to PersonPresenterFieldRowAdapter(
                {person, row -> row.populateAsStringField(MessageID.email, person.emailAddr,
                    inputType = CustomField.INPUT_TYPE_EMAIL, iconId = CustomField.ICON_EMAIL,
                    actionOnClick = CustomField.ACTION_EMAIL)},
                {person, row -> person.updateStringFieldFromRow(person::emailAddr, row)})
)


