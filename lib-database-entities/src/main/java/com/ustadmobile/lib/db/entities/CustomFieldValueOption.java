package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 55)
public class CustomFieldValueOption {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long customFieldValueOptionUid;

    //name of the option
    private String customFieldValueOptionName;

    //custom field uid
    private long customFieldValueOptionFieldUid;

    //icon string
    private String customFieldValueOptionIcon;

    //title string (message id)
    private int customFieldValueOptionMessageId;

    //active
    private boolean customFieldValueOptionActive;

    @UmSyncMasterChangeSeqNum
    private long customFieldValueOptionMCSN;

    @UmSyncLocalChangeSeqNum
    private long customFieldValueOptionLCSN;

    @UmSyncLastChangedBy
    private int customFieldValueOptionLCB;


    public long getCustomFieldValueOptionUid() {
        return customFieldValueOptionUid;
    }

    public void setCustomFieldValueOptionUid(long customFieldValueOptionUid) {
        this.customFieldValueOptionUid = customFieldValueOptionUid;
    }

    public String getCustomFieldValueOptionName() {
        return customFieldValueOptionName;
    }

    public void setCustomFieldValueOptionName(String customFieldValueOptionName) {
        this.customFieldValueOptionName = customFieldValueOptionName;
    }

    public long getCustomFieldValueOptionFieldUid() {
        return customFieldValueOptionFieldUid;
    }

    public void setCustomFieldValueOptionFieldUid(long customFieldValueOptionFieldUid) {
        this.customFieldValueOptionFieldUid = customFieldValueOptionFieldUid;
    }

    public String getCustomFieldValueOptionIcon() {
        return customFieldValueOptionIcon;
    }

    public void setCustomFieldValueOptionIcon(String customFieldValueOptionIcon) {
        this.customFieldValueOptionIcon = customFieldValueOptionIcon;
    }

    public int getCustomFieldValueOptionMessageId() {
        return customFieldValueOptionMessageId;
    }

    public void setCustomFieldValueOptionMessageId(int customFieldValueOptionMessageId) {
        this.customFieldValueOptionMessageId = customFieldValueOptionMessageId;
    }

    public long getCustomFieldValueOptionMCSN() {
        return customFieldValueOptionMCSN;
    }

    public void setCustomFieldValueOptionMCSN(long customFieldValueOptionMCSN) {
        this.customFieldValueOptionMCSN = customFieldValueOptionMCSN;
    }

    public long getCustomFieldValueOptionLCSN() {
        return customFieldValueOptionLCSN;
    }

    public void setCustomFieldValueOptionLCSN(long customFieldValueOptionLCSN) {
        this.customFieldValueOptionLCSN = customFieldValueOptionLCSN;
    }

    public int getCustomFieldValueOptionLCB() {
        return customFieldValueOptionLCB;
    }

    public void setCustomFieldValueOptionLCB(int customFieldValueOptionLCB) {
        this.customFieldValueOptionLCB = customFieldValueOptionLCB;
    }

    public boolean isCustomFieldValueOptionActive() {
        return customFieldValueOptionActive;
    }

    public void setCustomFieldValueOptionActive(boolean customFieldValueOptionActive) {
        this.customFieldValueOptionActive = customFieldValueOptionActive;
    }
}
