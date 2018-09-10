package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonDetailPresenterField {

    public static final int CUSTOM_FEILD_MIN_UID = 1000;

    public static final int FIELD_TYPE_HEADER = 1;

    public static final int FIELD_TYPE_FIELD = 2;

    /* Begin constants that represent Person core fields */
    public static final int PERSON_FIRST_NAMES = 1;

    public static final int PERSON_LAST_NAME = 2;

    public static final int PERSON_ATTENDANCE = 3;

    public static final int PERSON_CLASSES = 4;

    public static final int PERSON_FATHER_NAME_AND_PHONE_NUMBER = 5;

    @UmPrimaryKey(autoIncrement = true)
    private long personDetailPresenterFieldUid;

    private long fieldUid;

    private int fieldType;

    private int fieldIndex;

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
