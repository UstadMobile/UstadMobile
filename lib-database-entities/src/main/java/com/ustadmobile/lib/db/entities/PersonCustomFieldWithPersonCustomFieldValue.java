package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Represents Field Value pair for fields (custom)
 *
 */
public class PersonCustomFieldWithPersonCustomFieldValue extends PersonField {

    @UmEmbedded
    private PersonCustomFieldValue customFieldValue;

    public PersonCustomFieldValue getCustomFieldValue() {
        return customFieldValue;
    }

    public void setCustomFieldValue(PersonCustomFieldValue customFieldValue) {
        this.customFieldValue = customFieldValue;
    }
}
