package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 52)
public class SelQuestionOption {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long selQuestionOptionUid;

    private String optionText;

    private long selQuestionOptionQuestionUid;

    @UmSyncMasterChangeSeqNum
    private long selQuestionOptionMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long selQuestionOptionLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int selQuestionOptionLastChangedBy;

    private boolean optionActive;


    public boolean isOptionActive() {
        return optionActive;
    }

    public void setOptionActive(boolean optionActive) {
        this.optionActive = optionActive;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public long getSelQuestionOptionQuestionUid() {
        return selQuestionOptionQuestionUid;
    }

    public void setSelQuestionOptionQuestionUid(long selQuestionOptionQuestionUid) {
        this.selQuestionOptionQuestionUid = selQuestionOptionQuestionUid;
    }

    public long getSelQuestionOptionUid() {
        return selQuestionOptionUid;
    }

    public void setSelQuestionOptionUid(long selQuestionOptionUid) {
        this.selQuestionOptionUid = selQuestionOptionUid;
    }

    public long getSelQuestionOptionMasterChangeSeqNum() {
        return selQuestionOptionMasterChangeSeqNum;
    }

    public void setSelQuestionOptionMasterChangeSeqNum(long selQuestionOptionMasterChangeSeqNum) {
        this.selQuestionOptionMasterChangeSeqNum = selQuestionOptionMasterChangeSeqNum;
    }

    public long getSelQuestionOptionLocalChangeSeqNum() {
        return selQuestionOptionLocalChangeSeqNum;
    }

    public void setSelQuestionOptionLocalChangeSeqNum(long selQuestionOptionLocalChangeSeqNum) {
        this.selQuestionOptionLocalChangeSeqNum = selQuestionOptionLocalChangeSeqNum;
    }

    public int getSelQuestionOptionLastChangedBy() {
        return selQuestionOptionLastChangedBy;
    }

    public void setSelQuestionOptionLastChangedBy(int selQuestionOptionLastChangedBy) {
        this.selQuestionOptionLastChangedBy = selQuestionOptionLastChangedBy;
    }
}
