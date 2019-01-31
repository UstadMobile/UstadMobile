package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents a set of social nomination question eg: "Question set for Region A"
 */
@UmEntity(tableId = 25)
public class SocialNominationQuestionSet  {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long socialNominationQuestionSetUid;

    // The set title.
    private String title;

    @UmSyncMasterChangeSeqNum
    private long scheduleNominationQuestionSetMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long scheduleNominationQuestionSetLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int socialNominationQuestionSetLastChangedBy;

    public int getSocialNominationQuestionSetLastChangedBy() {
        return socialNominationQuestionSetLastChangedBy;
    }

    public void setSocialNominationQuestionSetLastChangedBy(int socialNominationQuestionSetLastChangedBy) {
        this.socialNominationQuestionSetLastChangedBy = socialNominationQuestionSetLastChangedBy;
    }

    public long getSocialNominationQuestionSetUid() {
        return socialNominationQuestionSetUid;
    }

    public void setSocialNominationQuestionSetUid(long socialNominationQuestionSetUid) {
        this.socialNominationQuestionSetUid = socialNominationQuestionSetUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getScheduleNominationQuestionSetMasterChangeSeqNum() {
        return scheduleNominationQuestionSetMasterChangeSeqNum;
    }

    public void setScheduleNominationQuestionSetMasterChangeSeqNum(long scheduleNominationQuestionSetMasterChangeSeqNum) {
        this.scheduleNominationQuestionSetMasterChangeSeqNum = scheduleNominationQuestionSetMasterChangeSeqNum;
    }

    public long getScheduleNominationQuestionSetLocalChangeSeqNum() {
        return scheduleNominationQuestionSetLocalChangeSeqNum;
    }

    public void setScheduleNominationQuestionSetLocalChangeSeqNum(long scheduleNominationQuestionSetLocalChangeSeqNum) {
        this.scheduleNominationQuestionSetLocalChangeSeqNum = scheduleNominationQuestionSetLocalChangeSeqNum;
    }
}
