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

/**
 * Get the CustomField and CustomFieldValue for a given field uid
 * @param fieldUid as per PERSON_FIELD_UID constants on PersonDetailPresenterField
 */
fun Person.populatePresenterFieldRow(presenterFieldRow: PresenterFieldRow): Boolean {
    val fieldUid = presenterFieldRow.presenterField?.fieldUid?.toInt() ?: return false
    presenterFieldRow.customField = personCoreFieldsMap[fieldUid]

    when(fieldUid) {
        PERSON_FIELD_UID_FIRST_NAMES -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueValue = firstNames)
        }
        PERSON_FIELD_UID_LAST_NAME -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueValue = lastName)
        }
        PERSON_FIELD_UID_BIRTHDAY -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueCustomFieldValueOptionUid = dateOfBirth)
        }
        PERSON_FIELD_UID_ADDRESS -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueValue = personAddress)
        }
        PERSON_FIELD_UID_USERNAME -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueValue = username)
        }
        PERSON_FIELD_UID_PHONE_NUMBER -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueValue = phoneNum)
        }
        PERSON_FIELD_UID_GENDER -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueCustomFieldValueOptionUid = gender.toLong())
            presenterFieldRow.customFieldOptions = personGenderCustomFieldValueOptions
        }
        PERSON_FIELD_UID_EMAIL -> {
            presenterFieldRow.customFieldValue = CustomFieldValue(customFieldValueValue = emailAddr)
        }
    }

    return presenterFieldRow.customField != null
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
        when(it.presenterField?.fieldUid?.toInt() ?: 0) {
            PERSON_FIELD_UID_FIRST_NAMES -> this.firstNames = it.customFieldValue?.customFieldValueValue
            PERSON_FIELD_UID_LAST_NAME -> this.lastName = it.customFieldValue?.customFieldValueValue
            PERSON_FIELD_UID_BIRTHDAY -> this.dateOfBirth = it.customFieldValue?.customFieldValueCustomFieldValueOptionUid ?: 0L
            PERSON_FIELD_UID_ADDRESS -> this.personAddress = it.customFieldValue?.customFieldValueValue
            PERSON_FIELD_UID_PHONE_NUMBER -> this.phoneNum = it.customFieldValue?.customFieldValueValue
            PERSON_FIELD_UID_USERNAME -> this.username = it.customFieldValue?.customFieldValueValue
            PERSON_FIELD_UID_GENDER -> this.gender = it.customFieldValue?.customFieldValueCustomFieldValueOptionUid?.toInt() ?: 0
            PERSON_FIELD_UID_EMAIL -> this.emailAddr = it.customFieldValue?.customFieldValueValue
        }
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

private val personCoreFieldsMap: Map<Int, CustomField> by lazy {
    mapOf(
        PERSON_FIELD_UID_FIRST_NAMES to CustomField(customFieldLabelMessageID = MessageID.first_names,
            customFieldType = FIELD_TYPE_TEXT),
        PERSON_FIELD_UID_LAST_NAME to CustomField(customFieldLabelMessageID =  MessageID.last_name,
                customFieldType = FIELD_TYPE_TEXT),
        PERSON_FIELD_UID_BIRTHDAY to CustomField(customFieldLabelMessageID = MessageID.birthday,
                customFieldType = FIELD_TYPE_DATE_SPINNER),
        PERSON_FIELD_UID_ADDRESS to CustomField(customFieldLabelMessageID = MessageID.home_address,
                customFieldType = FIELD_TYPE_TEXT),
        PERSON_FIELD_UID_USERNAME to CustomField(customFieldLabelMessageID = MessageID.username,
                customFieldType = FIELD_TYPE_TEXT),
        PERSON_FIELD_UID_PHONE_NUMBER to CustomField(customFieldLabelMessageID =  MessageID.phone_number,
                customFieldType = FIELD_TYPE_TEXT),
        PERSON_FIELD_UID_GENDER to CustomField(customFieldLabelMessageID = MessageID.gender_literal,
                customFieldType = FIELD_TYPE_DROPDOWN),
        PERSON_FIELD_UID_EMAIL to CustomField(customFieldLabelMessageID = MessageID.email,
                customFieldType = FIELD_TYPE_TEXT)
    )
}

