package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomField.Companion.FIELD_TYPE_PICTURE
import com.ustadmobile.lib.db.entities.CustomFieldValue
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.PERSON_FIELD_UID_PICTURE
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.PresenterFieldRow

fun PersonPicture.populatePresenterFields(presenterFields: List<PresenterFieldRow>) {
    presenterFields.filter { it.presenterField?.fieldUid?.toInt() == PERSON_FIELD_UID_PICTURE }
            .forEach {
                it.customField = CustomField(customFieldType = FIELD_TYPE_PICTURE)
                it.customFieldValue = CustomFieldValue(customFieldValueCustomFieldValueOptionUid = this.personPictureUid)
            }
}