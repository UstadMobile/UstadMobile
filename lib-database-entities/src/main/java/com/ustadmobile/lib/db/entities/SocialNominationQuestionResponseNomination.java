package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the student who is nominated for a specific question in the question set
 */
@UmEntity(tableId = 24)
public class SocialNominationQuestionResponseNomination {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionResponseNominationUid;

    private long socialNominationQuestionResponseNominationClazzMemberUid;

    //<-> SocialNominationQuestionResponse entity.
    private long socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId;


    @UmSyncMasterChangeSeqNum
    private long scheduleNominationQuestionResponseNominationMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long scheduleNominationQuestionResponseNominationLocalChangeSeqNum;

    public long getSocialNominationQuestionResponseNominationUid() {
        return socialNominationQuestionResponseNominationUid;
    }

    public void setSocialNominationQuestionResponseNominationUid(long socialNominationQuestionResponseNominationUid) {
        this.socialNominationQuestionResponseNominationUid = socialNominationQuestionResponseNominationUid;
    }

    public long getSocialNominationQuestionResponseNominationClazzMemberUid() {
        return socialNominationQuestionResponseNominationClazzMemberUid;
    }

    public void setSocialNominationQuestionResponseNominationClazzMemberUid(long socialNominationQuestionResponseNominationClazzMemberUid) {
        this.socialNominationQuestionResponseNominationClazzMemberUid = socialNominationQuestionResponseNominationClazzMemberUid;
    }

    public long getSocialNominationQuestionResponseNominationSocialNominationQuestionResponseUId() {
        return socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId;
    }

    public void setSocialNominationQuestionResponseNominationSocialNominationQuestionResponseUId(long socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId) {
        this.socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId = socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId;
    }

    public long getScheduleNominationQuestionResponseNominationMasterChangeSeqNum() {
        return scheduleNominationQuestionResponseNominationMasterChangeSeqNum;
    }

    public void setScheduleNominationQuestionResponseNominationMasterChangeSeqNum(long scheduleNominationQuestionResponseNominationMasterChangeSeqNum) {
        this.scheduleNominationQuestionResponseNominationMasterChangeSeqNum = scheduleNominationQuestionResponseNominationMasterChangeSeqNum;
    }

    public long getScheduleNominationQuestionResponseNominationLocalChangeSeqNum() {
        return scheduleNominationQuestionResponseNominationLocalChangeSeqNum;
    }

    public void setScheduleNominationQuestionResponseNominationLocalChangeSeqNum(long scheduleNominationQuestionResponseNominationLocalChangeSeqNum) {
        this.scheduleNominationQuestionResponseNominationLocalChangeSeqNum = scheduleNominationQuestionResponseNominationLocalChangeSeqNum;
    }
}
