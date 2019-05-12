package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonCustomField {

    @UmPrimaryKey(autoIncrement = true)
    private long personCustomFieldUid;

    private String fieldName;

    private int labelMessageCode;

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

    public int getLabelMessageCode() {
        return labelMessageCode;
    }

    public void setLabelMessageCode(int labelMessageCode) {
        this.labelMessageCode = labelMessageCode;
    }

    public String getFieldIcon() {
        return fieldIcon;
    }

    public void setFieldIcon(String fieldIcon) {
        this.fieldIcon = fieldIcon;
    }
}
