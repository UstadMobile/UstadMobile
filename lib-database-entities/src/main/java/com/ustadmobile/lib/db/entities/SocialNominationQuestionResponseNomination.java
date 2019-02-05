package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents the student who is nominated for a specific question in the question set
 */
@UmEntity(tableId = 24)
public class SocialNominationQuestionResponseNomination {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long socialNominationQuestionResponseNominationUid;

    private long socialNominationQuestionResponseNominationClazzMemberUid;

    //<-> SocialNominationQuestionResponse entity.
    private long socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId;

    //Added 31012019 sel TODO add to Migrations
    private boolean nominationActive;

    //Removed : TODO: add to Migrations
//    @UmSyncLastChangedBy
//    private int scheduleNominationQuestionResponseNominationLastChangedBy;

    //Renamed: TODO: add to Migrations
    @UmSyncMasterChangeSeqNum
    private long socialNominationQuestionResponseNominationMasterChangeSeqNum;
    //private long scheduleNominationQuestionResponseNominationMasterChangeSeqNum;

    //Renamed: TODO: add to Migrations
    @UmSyncLocalChangeSeqNum
    private long socialNominationQuestionResponseNominationLocalChangeSeqNum;
    //private long scheduleNominationQuestionResponseNominationLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int socialNominationQuestionResponseNominationLastChangedBy;

    public int getSocialNominationQuestionResponseNominationLastChangedBy() {
        return socialNominationQuestionResponseNominationLastChangedBy;
    }

    public void setSocialNominationQuestionResponseNominationLastChangedBy(int socialNominationQuestionResponseNominationLastChangedBy) {
        this.socialNominationQuestionResponseNominationLastChangedBy = socialNominationQuestionResponseNominationLastChangedBy;
    }

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


    public boolean isNominationActive() {
        return nominationActive;
    }

    public void setNominationActive(boolean nominationActive) {
        this.nominationActive = nominationActive;
    }

    public long getSocialNominationQuestionResponseNominationLocalChangeSeqNum() {
        return socialNominationQuestionResponseNominationLocalChangeSeqNum;
    }

    public void setSocialNominationQuestionResponseNominationLocalChangeSeqNum(long socialNominationQuestionResponseNominationLocalChangeSeqNum) {
        this.socialNominationQuestionResponseNominationLocalChangeSeqNum = socialNominationQuestionResponseNominationLocalChangeSeqNum;
    }

    public long getSocialNominationQuestionResponseNominationMasterChangeSeqNum() {
        return socialNominationQuestionResponseNominationMasterChangeSeqNum;
    }

    public void setSocialNominationQuestionResponseNominationMasterChangeSeqNum(long socialNominationQuestionResponseNominationMasterChangeSeqNum) {
        this.socialNominationQuestionResponseNominationMasterChangeSeqNum = socialNominationQuestionResponseNominationMasterChangeSeqNum;
    }
}
