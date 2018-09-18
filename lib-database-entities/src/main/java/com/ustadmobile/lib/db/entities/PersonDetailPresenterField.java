package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * This entity represents all the fields including the headers and extra information that will
 * get represented on the view. This includes the field UID (not PK), the type of field
 * (header or text/date/drop-down/phone/etc), the index for order and view flags.
 *
 */
@UmEntity
public class PersonDetailPresenterField {

    /* Begin constants that represent Person core fields */

    public static final int PERSON_FIELD_UID_FULL_NAME = 1;

    public static final int PERSON_FIELD_UID_FIRST_NAMES = 2;

    public static final int PERSON_FIELD_UID_LAST_NAME = 3;

    public static final int PERSON_FIELD_UID_ATTENDANCE = 4;

    public static final int PERSON_FIELD_UID_CLASSES = 5;

    public static final int PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER = 6;

    public static final int PERSON_FIELD_UID_FATHER_NAME = 7;

    public static final int PERSON_FIELD_UID_FATHER_NUMBER = 8;

    public static final int PERSON_FIELD_UID_MOTHER_NAME = 9;

    public static final int PERSON_FIELD_UID_MOTHER_NUMBER = 10;

    public static final int PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER = 11;

    public static final int PERSON_FIELD_UID_BIRTHDAY = 12;

    public static final int PERSON_FIELD_UID_ADDRESS = 13;

    /* Field Uid constants for Person Custom fields begin at this value */
    public static final int CUSTOM_FIELD_MIN_UID = 1000;

    //PK
    @UmPrimaryKey(autoIncrement = true)
    private long personDetailPresenterFieldUid;

    //The field id associated with PersonField. For Core Fields it is as above. For Custom
    // it starts from 1000 ++
    private long fieldUid;

    //The type of this  field (header or field)
    private int fieldType;

    //The index used in ordering things
    private int fieldIndex;

    //The label of the field used in the views.
    private int labelMessageId;

    //The field icon used in the view.
    private String fieldIcon;

    //The Label of the header (if applicable)
    private int headerMessageId;

    //If this presenter field is visible on PersonDetail
    private boolean viewModeVisible;

    //If this presenter field is visible on PersonEdit/PersonNew
    private boolean editModeVisible;

    //Set if its uneditable
    private boolean readyOnly;

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

    //sometimes we want to display a field but not be able to edit it. This is the flag for that.
    public boolean isReadyOnly() {
        return readyOnly;
    }


    public void setReadyOnly(boolean readyOnly) {
        this.readyOnly = readyOnly;
    }

    public boolean isViewModeVisible() {
        return viewModeVisible;
    }

    public void setViewModeVisible(boolean viewModeVisible) {
        this.viewModeVisible = viewModeVisible;
    }

    public boolean isEditModeVisible() {
        return editModeVisible;
    }

    public void setEditModeVisible(boolean editModeVisible) {
        this.editModeVisible = editModeVisible;
    }

    public long getPersonDetailPresenterFieldUid() {
        return personDetailPresenterFieldUid;
    }

    public void setPersonDetailPresenterFieldUid(long personDetailPresenterFieldUid) {
        this.personDetailPresenterFieldUid = personDetailPresenterFieldUid;
    }

    public long getFieldUid() {
        return fieldUid;
    }

    public void setFieldUid(long fieldUid) {
        this.fieldUid = fieldUid;
    }

    public int getFieldType() {
        return fieldType;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public int getHeaderMessageId() {
        return headerMessageId;
    }

    public void setHeaderMessageId(int headerMessageId) {
        this.headerMessageId = headerMessageId;
    }
}
