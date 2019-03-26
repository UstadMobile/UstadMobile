package com.ustadmobile.lib.db.entities;

/**
 * POJO Representation of a custom field. This is populated from both PersonField as well as
 * ClazzCustomField
 */
public class CustomFieldWrapper {
    public static final int FIELD_TYPE_DROPDOWN = 2;
    public static final int FIELD_TYPE_TEXT = 1;

    String fieldName;
    String fieldType;
    String defaultValue;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
