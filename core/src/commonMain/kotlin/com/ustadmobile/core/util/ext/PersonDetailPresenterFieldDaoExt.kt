package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_ADDRESS
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_BIRTHDAY
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_FIRST_NAMES
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_LAST_NAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_USERNAME
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_HEADER

private val PERSON_CORE_FIELDS_DEFAULT = listOf(
        PERSON_FIELD_UID_FIRST_NAMES,
        PERSON_FIELD_UID_LAST_NAME,
        PERSON_FIELD_UID_BIRTHDAY,
        PERSON_FIELD_UID_ADDRESS,
        PERSON_FIELD_UID_USERNAME,
        PERSON_FIELD_UID_PHONE_NUMBER)


fun PersonDetailPresenterFieldDao.preloadCoreFields() {
    insertListAbortConflicts(
    listOf(PersonDetailPresenterField(personDetailPresenterFieldUid = 50,
            fieldType = TYPE_HEADER, headerMessageId = MessageID.basic_details,
        fieldIndex = 0)) +
            PERSON_CORE_FIELDS_DEFAULT.mapIndexed { index, fieldUid ->
        PersonDetailPresenterField(personDetailPresenterFieldUid = fieldUid.toLong(),
                fieldType = TYPE_FIELD, fieldUid = fieldUid.toLong(),fieldIndex = index + 1)
    })
}