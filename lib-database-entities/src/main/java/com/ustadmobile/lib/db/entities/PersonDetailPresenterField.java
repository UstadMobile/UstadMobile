package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonDetailPresenterField {

    public static final int CUSTOM_FEILD_MIN_UID = 1000;

    public static final int FIELD_TYPE_HEADER = 1;

    public static final int FIELD_TYPE_FIELD = 2;

    /* Begin constants that represent Person core fields */
    public static final int PERSON_FIELD_UID_FIRST_NAMES = 1;

    public static final int PERSON_FIELD_UID_LAST_NAME = 2;

    public static final int PERSON_FIELD_UID_ATTENDANCE = 3;

    public static final int PERSON_FIELD_UID_CLASSES = 4;

    public static final int PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER = 5;

    public static final int PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER = 6;

    public static final int PERSON_FIELD_UID_BIRTHDAY = 7;

    @UmPrimaryKey(autoIncrement = true)
    private long personDetailPresenterFieldUid;

    //The field id associated with PersonCustomField. For Core Fields it is as above. For Custom
    // it starts from 1000 ++
    private long fieldUid;

    //The type of this  field (header or field)
    private int fieldType;

    //The index used in ordering things
    private int fieldIndex;

    //The Label of the headder (if applicable)
    private int headerMessageId;

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
