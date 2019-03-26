package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 56)
public class CustomField {

    public static final int FIELD_TYPE_TEXT = 1;
    public static final int FIELD_TYPE_DROPDOWN = 2;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long customFieldUid;

    //name of field eg: Department
    private String customFieldName;
    //Name alt of field eg : in Arabic
    private String customFieldNameAlt;
    //Title in message id
    private int customFieldLabelMessageID;
    //Icon string
    private String customFieldIcon;
    //Type: dropdown or text. Flags defined here
    private int customFieldType;
    //Entity type table id (eg: Class or Person)
    private int customFieldEntityType;
    //if false it is considered not active and it wont show up in the app. effectively "deleted"
    private boolean customFieldActive;

    @UmSyncMasterChangeSeqNum
    private long customFieldMCSN;

    @UmSyncLocalChangeSeqNum
    private long customFieldLCSN;

    @UmSyncLastChangedBy
    private int customFieldLCB;


    public long getCustomFieldUid() {
        return customFieldUid;
    }

    public void setCustomFieldUid(long customFieldUid) {
        this.customFieldUid = customFieldUid;
    }

    public String getCustomFieldName() {
        return customFieldName;
    }

    public void setCustomFieldName(String customFieldName) {
        this.customFieldName = customFieldName;
    }

    public String getCustomFieldNameAlt() {
        return customFieldNameAlt;
    }

    public void setCustomFieldNameAlt(String customFieldNameAlt) {
        this.customFieldNameAlt = customFieldNameAlt;
    }

    public int getCustomFieldLabelMessageID() {
        return customFieldLabelMessageID;
    }

    public void setCustomFieldLabelMessageID(int customFieldLabelMessageID) {
        this.customFieldLabelMessageID = customFieldLabelMessageID;
    }

    public String getCustomFieldIcon() {
        return customFieldIcon;
    }

    public void setCustomFieldIcon(String customFieldIcon) {
        this.customFieldIcon = customFieldIcon;
    }

    public long getCustomFieldMCSN() {
        return customFieldMCSN;
    }

    public void setCustomFieldMCSN(long customFieldMCSN) {
        this.customFieldMCSN = customFieldMCSN;
    }

    public long getCustomFieldLCSN() {
        return customFieldLCSN;
    }

    public void setCustomFieldLCSN(long customFieldLCSN) {
        this.customFieldLCSN = customFieldLCSN;
    }

    public int getCustomFieldLCB() {
        return customFieldLCB;
    }

    public void setCustomFieldLCB(int customFieldLCB) {
        this.customFieldLCB = customFieldLCB;
    }

    public int getCustomFieldType() {
        return customFieldType;
    }

    public void setCustomFieldType(int customFieldType) {
        this.customFieldType = customFieldType;
    }

    public int getCustomFieldEntityType() {
        return customFieldEntityType;
    }

    public void setCustomFieldEntityType(int customFieldEntityType) {
        this.customFieldEntityType = customFieldEntityType;
    }

    public boolean isCustomFieldActive() {
        return customFieldActive;
    }

    public void setCustomFieldActive(boolean customFieldActive) {
        this.customFieldActive = customFieldActive;
    }
}
