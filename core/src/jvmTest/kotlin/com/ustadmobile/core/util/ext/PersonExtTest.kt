package com.ustadmobile.core.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD
import com.ustadmobile.lib.db.entities.PresenterFieldRow
import org.junit.Assert
import org.junit.Test

class PersonExtTest {

    @Test
    fun givenPersonAndListOfCustomFields_whenConvertedToListAndBack_thenShouldBeTheSame() {
        val testCustomFields = listOf<PresenterFieldRow>(
                PresenterFieldRow(PersonDetailPresenterField(fieldType = TYPE_FIELD,
                    fieldUid = PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES.toLong())),
                PresenterFieldRow(PersonDetailPresenterField(fieldType =TYPE_FIELD),
                    CustomField(customFieldType = CustomField.FIELD_TYPE_TEXT,
                        customFieldLabelMessageID = MessageID.class_id))
        )
        val testPerson = Person().apply {
            firstNames = "Bob"
        }

        val asFieldRowList = testPerson.populatePresenterFields(testCustomFields)
        asFieldRowList.first { it.presenterField?.fieldUid == PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES.toLong() }
                .customFieldValue?.customFieldValueValue = "Joe"

        testPerson.updateFromFieldList(asFieldRowList)

        Assert.assertEquals("Name is updated from PresenterFieldRow", "Joe",
                testPerson.firstNames)
    }

}