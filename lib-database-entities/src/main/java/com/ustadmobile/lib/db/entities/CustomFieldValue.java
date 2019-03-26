package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 57)
public class CustomFieldValue {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long customFieldValueUid;

    //custom field uid
    private long customFieldValueFieldUid;

    //Entity uid (eg clazz uid / person uid)
    private long customFieldValueEntityUid;

    //value as String
    private String customFieldValueValue;

    @UmSyncMasterChangeSeqNum
    private long customFieldValueMCSN;

    @UmSyncLocalChangeSeqNum
    private long customFieldValueLCSN;

    @UmSyncLastChangedBy
    private int customFieldValueLCB;


    public long getCustomFieldValueUid() {
        return customFieldValueUid;
    }

    public void setCustomFieldValueUid(long customFieldValueUid) {
        this.customFieldValueUid = customFieldValueUid;
    }

    public long getCustomFieldValueFieldUid() {
        return customFieldValueFieldUid;
    }

    public void setCustomFieldValueFieldUid(long customFieldValueFieldUid) {
        this.customFieldValueFieldUid = customFieldValueFieldUid;
    }

    public long getCustomFieldValueEntityUid() {
        return customFieldValueEntityUid;
    }

    public void setCustomFieldValueEntityUid(long customFieldValueEntityUid) {
        this.customFieldValueEntityUid = customFieldValueEntityUid;
    }

    public String getCustomFieldValueValue() {
        return customFieldValueValue;
    }

    public void setCustomFieldValueValue(String customFieldValueValue) {
        this.customFieldValueValue = customFieldValueValue;
    }

    public long getCustomFieldValueMCSN() {
        return customFieldValueMCSN;
    }

    public void setCustomFieldValueMCSN(long customFieldValueMCSN) {
        this.customFieldValueMCSN = customFieldValueMCSN;
    }

    public long getCustomFieldValueLCSN() {
        return customFieldValueLCSN;
    }

    public void setCustomFieldValueLCSN(long customFieldValueLCSN) {
        this.customFieldValueLCSN = customFieldValueLCSN;
    }

    public int getCustomFieldValueLCB() {
        return customFieldValueLCB;
    }

    public void setCustomFieldValueLCB(int customFieldValueLCB) {
        this.customFieldValueLCB = customFieldValueLCB;
    }
}
