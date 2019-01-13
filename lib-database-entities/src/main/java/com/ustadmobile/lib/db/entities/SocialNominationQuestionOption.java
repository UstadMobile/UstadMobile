package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 52)
public class SocialNominationQuestionOption {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long socialNominationQuestionOptionUid;

    private String optionText;

    private long selQuestionOptionQuestionUid;

    @UmSyncMasterChangeSeqNum
    private long socialNominationQuestionOptionMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long socialNominationQuestionOptionLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int socialNominationQuestionOptionLastChangedBy;

    private boolean optionActive;


    public int getSocialNominationQuestionOptionLastChangedBy() {
        return socialNominationQuestionOptionLastChangedBy;
    }

    public void setSocialNominationQuestionOptionLastChangedBy(int socialNominationQuestionOptionLastChangedBy) {
        this.socialNominationQuestionOptionLastChangedBy = socialNominationQuestionOptionLastChangedBy;
    }

    public boolean isOptionActive() {
        return optionActive;
    }

    public void setOptionActive(boolean optionActive) {
        this.optionActive = optionActive;
    }

    public long getSocialNominationQuestionOptionUid() {
        return socialNominationQuestionOptionUid;
    }

    public void setSocialNominationQuestionOptionUid(long socialNominationQuestionOptionUid) {
        this.socialNominationQuestionOptionUid = socialNominationQuestionOptionUid;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public long getSocialNominationQuestionOptionMasterChangeSeqNum() {
        return socialNominationQuestionOptionMasterChangeSeqNum;
    }

    public void setSocialNominationQuestionOptionMasterChangeSeqNum(long socialNominationQuestionOptionMasterChangeSeqNum) {
        this.socialNominationQuestionOptionMasterChangeSeqNum = socialNominationQuestionOptionMasterChangeSeqNum;
    }

    public long getSocialNominationQuestionOptionLocalChangeSeqNum() {
        return socialNominationQuestionOptionLocalChangeSeqNum;
    }

    public void setSocialNominationQuestionOptionLocalChangeSeqNum(long socialNominationQuestionOptionLocalChangeSeqNum) {
        this.socialNominationQuestionOptionLocalChangeSeqNum = socialNominationQuestionOptionLocalChangeSeqNum;
    }

    public long getSelQuestionOptionQuestionUid() {
        return selQuestionOptionQuestionUid;
    }

    public void setSelQuestionOptionQuestionUid(long selQuestionOptionQuestionUid) {
        this.selQuestionOptionQuestionUid = selQuestionOptionQuestionUid;
    }
}
