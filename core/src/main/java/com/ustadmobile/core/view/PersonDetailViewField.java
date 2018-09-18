package com.ustadmobile.core.view;

import java.util.List;
import java.util.Map;

/**
 * Class representing a person's detail in the View. This isn't a View but a component of
 * constructing the PersonDetail/Edit View.
 *
 * This is the class that the View will be
 * made aware of. Part of Person and PersonDetailCustomField
 * Used in PersonDetail and PersonDetailEdit.
 *
 * We assign every field an id, its type, label and options.
 *
 */
public class PersonDetailViewField {

    /* All of the types */

    public static final int FIELD_TYPE_HEADER = 1;

    public static final int FIELD_TYPE_FIELD = 2;

    public static final int FIELD_TYPE_TEXT = 3;

    public static final int FIELD_TYPE_DROPDOWN = 4;

    public static final int FIELD_TYPE_PHONE_NUMBER = 5;

    public static final int FIELD_TYPE_DATE = 6;

    public PersonDetailViewField(int fieldType, int messageLabel, String iconName) {
        this.fieldType = fieldType;
        this.messageLabel = messageLabel;
        this.iconName = iconName;
    }

    public PersonDetailViewField(int fieldType, int messageLabel, String actionParam,
                                 String iconName) {
        this.fieldType = fieldType;
        this.messageLabel = messageLabel;
        this.iconName = iconName;
        this.actionParam = actionParam;
    }

    //The type of field
    private int fieldType;

    //Message Label Id based on MessageID which is linked to strings xml.
    private int messageLabel;

    //The iconName drawable resource string
    private String iconName;

    //Any secondary action parameters like phone number / email / link ?
    private String actionParam;

    //Any options specifically for drop down types. Can be null for other fieldTypes.
    private List<Map.Entry<Object, String>> fieldOptions;


    public String getActionParam() {
        return actionParam;
    }

    public void setActionParam(String actionParam) {
        this.actionParam = actionParam;
    }

    public int getFieldType() {
        return fieldType;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public int getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(int messageLabel) {
        this.messageLabel = messageLabel;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public List<Map.Entry<Object, String>> getFieldOptions() {
        return fieldOptions;
    }

    public void setFieldOptions(List<Map.Entry<Object, String>> fieldOptions) {
        this.fieldOptions = fieldOptions;
    }
}
