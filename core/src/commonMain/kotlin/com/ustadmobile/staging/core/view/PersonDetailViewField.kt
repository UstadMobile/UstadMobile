package com.ustadmobile.core.view

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
class PersonDetailViewField {

    /* All of the types */

    //The type of field
    var fieldType: Int = 0
        private set

    //Message Label Id based on MessageID which is linked to strings xml.
    var messageLabel: Int = 0
        private set

    //The iconName drawable resource string
    var iconName: String? = null
        private set

    //Any secondary action parameters like phone number / email / link ?
    /*
        GETTERS AND SETTERS
     */

    var actionParam: String? = null

    /**
     * Gets the view ready for the person's field.
     *
     * @param fieldType The field type
     * @param messageLabel  The label
     * @param iconName  The icon name
     */
    constructor(fieldType: Int, messageLabel: Int, iconName: String?) {
        this.fieldType = fieldType
        this.messageLabel = messageLabel
        this.iconName = iconName
    }

    /**
     * Gets the view ready for the person's fields.
     *
     * @param fieldType The field type
     * @param messageLabel  The label
     * @param actionParam   The action parameter
     * @param iconName      The icon name
     */
    constructor(fieldType: Int, messageLabel: Int, actionParam: String?,
                iconName: String?) {
        this.fieldType = fieldType
        this.messageLabel = messageLabel
        this.iconName = iconName
        this.actionParam = actionParam
    }

}
