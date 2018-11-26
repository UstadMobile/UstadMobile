package com.ustadmobile.lib.db.entities;


import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * This entity represents the custom field value associated with any custom field for a given
 * person Uid.
 *
 * A custom field is a field in PersonField.
 */
@UmEntity(tableId = 18)
public class PersonCustomFieldValue {

    //PK
    @UmPrimaryKey(autoGenerateSyncable = true)
    private long personCustomFieldValueUid;

    //The Custom field's uid
    private long personCustomFieldValuePersonCustomFieldUid;

    //The person associated uid
    private long personCustomFieldValuePersonUid;

    //The value itself
    private String fieldValue;

    @UmSyncMasterChangeSeqNum
    private long personCustomFieldValueMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long personCustomFieldValueLocalChangeSeqNum;


    public long getPersonCustomFieldValueMasterChangeSeqNum() {
        return personCustomFieldValueMasterChangeSeqNum;
    }

    public void setPersonCustomFieldValueMasterChangeSeqNum(long personCustomFieldValueMasterChangeSeqNum) {
        this.personCustomFieldValueMasterChangeSeqNum = personCustomFieldValueMasterChangeSeqNum;
    }

    public long getPersonCustomFieldValueLocalChangeSeqNum() {
        return personCustomFieldValueLocalChangeSeqNum;
    }

    public void setPersonCustomFieldValueLocalChangeSeqNum(long personCustomFieldValueLocalChangeSeqNum) {
        this.personCustomFieldValueLocalChangeSeqNum = personCustomFieldValueLocalChangeSeqNum;
    }

    public long getPersonCustomFieldValueUid() {
        return personCustomFieldValueUid;
    }

    public void setPersonCustomFieldValueUid(long personCustomFieldValueUid) {
        this.personCustomFieldValueUid = personCustomFieldValueUid;
    }

    public long getPersonCustomFieldValuePersonCustomFieldUid() {
        return personCustomFieldValuePersonCustomFieldUid;
    }

    public void setPersonCustomFieldValuePersonCustomFieldUid(
            long personCustomFieldValuePersonCustomFieldUid) {
        this.personCustomFieldValuePersonCustomFieldUid = personCustomFieldValuePersonCustomFieldUid;
    }

    public long getPersonCustomFieldValuePersonUid() {
        return personCustomFieldValuePersonUid;
    }

    public void setPersonCustomFieldValuePersonUid(long personCustomFieldValuePersonUid) {
        this.personCustomFieldValuePersonUid = personCustomFieldValuePersonUid;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
