package com.ustadmobile.core.view;

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

    //The type of field
    private int fieldType;

    //Message Label Id based on MessageID which is linked to strings xml.
    private int messageLabel;

    //The iconName drawable resource string
    private String iconName;

    //Any secondary action parameters like phone number / email / link ?
    private String actionParam;

    /**
     * Gets the view ready for the person's field.
     *
     * @param fieldType The field type
     * @param messageLabel  The label
     * @param iconName  The icon name
     */
    public PersonDetailViewField(int fieldType, int messageLabel, String iconName) {
        this.fieldType = fieldType;
        this.messageLabel = messageLabel;
        this.iconName = iconName;
    }

    /**
     * Gets the view ready for the person's fields.
     *
     * @param fieldType The field type
     * @param messageLabel  The label
     * @param actionParam   The action parameter
     * @param iconName      The icon name
     */
    public PersonDetailViewField(int fieldType, int messageLabel, String actionParam,
                                 String iconName) {
        this.fieldType = fieldType;
        this.messageLabel = messageLabel;
        this.iconName = iconName;
        this.actionParam = actionParam;
    }

    /*
        GETTERS AND SETTERS
     */

    public String getActionParam() {
        return actionParam;
    }

    public int getFieldType() {
        return fieldType;
    }

    public int getMessageLabel() {
        return messageLabel;
    }

    public String getIconName() {
        return iconName;
    }

}
