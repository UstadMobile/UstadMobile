package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonCustomField {

    @UmPrimaryKey(autoIncrement = true)
    private long personCustomFieldUid;

    //Any extra field names that isn't used in the views.
    private String fieldName;

    //The label of the field used in the views.
    private int labelMessageId;

    //The field icon used in the view.
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
