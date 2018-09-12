package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class PersonEditPresenterField {

    @UmPrimaryKey(autoIncrement = true)
    private long personEditPresenterFieldUid;

    //The field id associated with PersonCustomField. For Core Fields it is as above. For Custom
    // it starts from 1000 ++
    private long personEditPresenterFieldFieldUid;

    //The type of this  field (header or field)
    private int personEditPresenterFieldType;

    //The index used in ordering things
    private int personEditPresenterFieldIndex;

    //The Label of the headder (if applicable) for group names only
    private int personEditPresenterFieldHeaderMessageId;

    public long getPersonEditPresenterFieldUid() {
        return personEditPresenterFieldUid;
    }

    public void setPersonEditPresenterFieldUid(long personEditPresenterFieldUid) {
        this.personEditPresenterFieldUid = personEditPresenterFieldUid;
    }

    public long getPersonEditPresenterFieldFieldUid() {
        return personEditPresenterFieldFieldUid;
    }

    public void setPersonEditPresenterFieldFieldUid(long personEditPresenterFieldFieldUid) {
        this.personEditPresenterFieldFieldUid = personEditPresenterFieldFieldUid;
    }

    public int getPersonEditPresenterFieldType() {
        return personEditPresenterFieldType;
    }

    public void setPersonEditPresenterFieldType(int personEditPresenterFieldType) {
        this.personEditPresenterFieldType = personEditPresenterFieldType;
    }

    public int getPersonEditPresenterFieldIndex() {
        return personEditPresenterFieldIndex;
    }

    public void setPersonEditPresenterFieldIndex(int personEditPresenterFieldIndex) {
        this.personEditPresenterFieldIndex = personEditPresenterFieldIndex;
    }

    public int getPersonEditPresenterFieldHeaderMessageId() {
        return personEditPresenterFieldHeaderMessageId;
    }

    public void setPersonEditPresenterFieldHeaderMessageId(int personEditPresenterFieldHeaderMessageId) {
        this.personEditPresenterFieldHeaderMessageId = personEditPresenterFieldHeaderMessageId;
    }
}
