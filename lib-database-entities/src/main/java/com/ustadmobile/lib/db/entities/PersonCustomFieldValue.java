package com.ustadmobile.lib.db.entities;


import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonCustomFieldValue {

    @UmPrimaryKey(autoIncrement = true)
    private long personCustomFieldValueUid;

    private long personCustomFieldValuePersonCustomFieldUid;

    private long personCustomFieldValuePersonUid;

    private String fieldValue;

    public long getPersonCustomFieldValueUid() {
        return personCustomFieldValueUid;
    }

    public void setPersonCustomFieldValueUid(long personCustomFieldValueUid) {
        this.personCustomFieldValueUid = personCustomFieldValueUid;
    }

    public long getPersonCustomFieldValuePersonCustomFieldUid() {
        return personCustomFieldValuePersonCustomFieldUid;
    }

    public void setPersonCustomFieldValuePersonCustomFieldUid(long personCustomFieldValuePersonCustomFieldUid) {
        this.personCustomFieldValuePersonCustomFieldUid = personCustomFieldValuePersonCustomFieldUid;
    }

    public long getPersonCustomFieldValuePersonUid() {
        return personCustomFieldValuePersonUid;
    }

    public void setPersonCustomFieldValuePersonUid(long personCustomFieldValuePersonUid) {
        this.personCustomFieldValuePersonUid = personCustomFieldValuePersonUid;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
