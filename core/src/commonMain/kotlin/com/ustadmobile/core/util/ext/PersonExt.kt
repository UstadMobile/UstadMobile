package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME

/**
 * Get the CustomField and CustomFieldValue for a given field uid
 * @param fieldUid as per PERSON_FIELD_UID constants on PersonDetailPresenterField
 */
fun Person.getCustomFieldValue(fieldUid: Int): Pair<CustomField, CustomFieldValue>? {
    val customField = personCoreFieldsMap[fieldUid] ?: return null
    return when(fieldUid) {
        PERSON_FIELD_UID_FIRST_NAMES -> Pair(customField, CustomFieldValue(customFieldValueValue = firstNames))
        PERSON_FIELD_UID_LAST_NAME -> Pair(customField, CustomFieldValue(customFieldValueValue = lastName))
        else -> null
    }
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
fun Person.asPresenterFieldList(presenterFields: List<PresenterFieldRow>): List<PresenterFieldRow> {
    return presenterFields.map {
        if(it.customField == null
                && it.presenterField?.fieldType == PersonDetailPresenterField.TYPE_FIELD) {
            val fieldAndValue = getCustomFieldValue(it.presenterField?.fieldUid?.toInt() ?: 0)
            PresenterFieldRow(it.presenterField, fieldAndValue?.first, fieldAndValue?.second)
        }else {
            it
        }
    }
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
        }
    }
}

private val personCoreFieldsMap: Map<Int, CustomField> by lazy {
    mapOf(PERSON_FIELD_UID_FIRST_NAMES to CustomField(customFieldLabelMessageID = MessageID.first_names))
}

