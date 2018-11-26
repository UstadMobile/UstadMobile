package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the students response to a specific question in the question set
 */
@UmEntity(tableId = 23)
public class SocialNominationQuestionResponse {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long socialNominationQuestionResponseUid;

    // -> SocialNominationQuestionSetResponse
    private long socialNominationQuestionResponseSocialNominationQuestionSetResponseUid;

    @UmSyncMasterChangeSeqNum
    private long scheduleNominationQuestionResponseMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long scheduleNominationQuestionResponseLocalChangeSeqNum;

    public long getSocialNominationQuestionResponseUid() {
        return socialNominationQuestionResponseUid;
    }

    public void setSocialNominationQuestionResponseUid(long socialNominationQuestionResponseUid) {
        this.socialNominationQuestionResponseUid = socialNominationQuestionResponseUid;
    }

    public long getSocialNominationQuestionResponseSocialNominationQuestionSetResponseUid() {
        return socialNominationQuestionResponseSocialNominationQuestionSetResponseUid;
    }

    public void setSocialNominationQuestionResponseSocialNominationQuestionSetResponseUid(long socialNominationQuestionResponseSocialNominationQuestionSetResponseUid) {
        this.socialNominationQuestionResponseSocialNominationQuestionSetResponseUid = socialNominationQuestionResponseSocialNominationQuestionSetResponseUid;
    }

    public long getScheduleNominationQuestionResponseMasterChangeSeqNum() {
        return scheduleNominationQuestionResponseMasterChangeSeqNum;
    }

    public void setScheduleNominationQuestionResponseMasterChangeSeqNum(long scheduleNominationQuestionResponseMasterChangeSeqNum) {
        this.scheduleNominationQuestionResponseMasterChangeSeqNum = scheduleNominationQuestionResponseMasterChangeSeqNum;
    }

    public long getScheduleNominationQuestionResponseLocalChangeSeqNum() {
        return scheduleNominationQuestionResponseLocalChangeSeqNum;
    }

    public void setScheduleNominationQuestionResponseLocalChangeSeqNum(long scheduleNominationQuestionResponseLocalChangeSeqNum) {
        this.scheduleNominationQuestionResponseLocalChangeSeqNum = scheduleNominationQuestionResponseLocalChangeSeqNum;
    }
}
