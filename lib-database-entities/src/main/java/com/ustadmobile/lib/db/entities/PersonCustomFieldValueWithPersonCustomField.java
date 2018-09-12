package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * TODO: Remove this. This is replaced by PersonCustomFieldWithPersonCustomFieldValue (gasp)
 */
public class PersonCustomFieldValueWithPersonCustomField extends PersonCustomFieldValue {

    @UmEmbedded
    private PersonCustomField customField;

    public PersonCustomField getCustomField() {
        return customField;
    }

    public void setCustomField(PersonCustomField customField) {
        this.customField = customField;
    }
}
