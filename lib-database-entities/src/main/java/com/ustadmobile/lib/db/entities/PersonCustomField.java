package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonCustomField {

    @UmPrimaryKey(autoIncrement = true)
    private long personCustomFieldUid;

    //Any extra field names that isn't used in the display of things yet.
    private String fieldName;

    private int labelMessageId;

    private String fieldIcon;

    public long getPersonCustomFieldUid() {
        return personCustomFieldUid;
    }

    public void setPersonCustomFieldUid(long personCustomFieldUid) {
        this.personCustomFieldUid = personCustomFieldUid;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getLabelMessageId() {
        return labelMessageId;
    }

    public void setLabelMessageId(int labelMessageId) {
        this.labelMessageId = labelMessageId;
    }

    public String getFieldIcon() {
        return fieldIcon;
    }

    public void setFieldIcon(String fieldIcon) {
        this.fieldIcon = fieldIcon;
    }
}
