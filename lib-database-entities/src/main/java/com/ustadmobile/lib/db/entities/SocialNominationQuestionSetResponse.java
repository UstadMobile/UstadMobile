package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * Represents one run through of a question set for one particular student.
 */
@UmEntity(tableId = 27)
public class SocialNominationQuestionSetResponse {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long socialNominationQuestionSetResposeUid;

    //-> SocialNominationQuestionSet - The Question Set
    private long socialNominationQuestionSetResponseSocialNominationQuestionSetUid;

    //clazz member doing this - The student (Class Member) doing this.
    private long socialNominationQuestionSetResponseClazzMemberUid;

    //start time
    private long socialNominationQuestionSetResponseStartTime;

    //finish time
    private long socialNominationQuestionSetResponseFinishTime;

    //total Response Recognition percentage. - to be calculated on device (not database).
    private float socialNominationQuestionSetResponseRecognitionPercentage;


    @UmSyncMasterChangeSeqNum
    private long scheduleNominationQuestionSetResponseMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long scheduleNominationQuestionSetResponseLocalChangeSeqNum;

    @UmSyncLastChangedBy
    private int socialNominationQuestionSetResponseLastChangedBy;


    public long getSocialNominationQuestionSetResposeUid() {
        return socialNominationQuestionSetResposeUid;
    }

    public void setSocialNominationQuestionSetResposeUid(long socialNominationQuestionSetResposeUid) {
        this.socialNominationQuestionSetResposeUid = socialNominationQuestionSetResposeUid;
    }

    public long getSocialNominationQuestionSetResponseSocialNominationQuestionSetUid() {
        return socialNominationQuestionSetResponseSocialNominationQuestionSetUid;
    }

    public void setSocialNominationQuestionSetResponseSocialNominationQuestionSetUid(long socialNominationQuestionSetResponseSocialNominationQuestionSetUid) {
        this.socialNominationQuestionSetResponseSocialNominationQuestionSetUid = socialNominationQuestionSetResponseSocialNominationQuestionSetUid;
    }

    public long getSocialNominationQuestionSetResponseClazzMemberUid() {
        return socialNominationQuestionSetResponseClazzMemberUid;
    }

    public void setSocialNominationQuestionSetResponseClazzMemberUid(long socialNominationQuestionSetResponseClazzMemberUid) {
        this.socialNominationQuestionSetResponseClazzMemberUid = socialNominationQuestionSetResponseClazzMemberUid;
    }

    public long getSocialNominationQuestionSetResponseStartTime() {
        return socialNominationQuestionSetResponseStartTime;
    }

    public void setSocialNominationQuestionSetResponseStartTime(long socialNominationQuestionSetResponseStartTime) {
        this.socialNominationQuestionSetResponseStartTime = socialNominationQuestionSetResponseStartTime;
    }

    public long getSocialNominationQuestionSetResponseFinishTime() {
        return socialNominationQuestionSetResponseFinishTime;
    }

    public void setSocialNominationQuestionSetResponseFinishTime(long socialNominationQuestionSetResponseFinishTime) {
        this.socialNominationQuestionSetResponseFinishTime = socialNominationQuestionSetResponseFinishTime;
    }

    public float getSocialNominationQuestionSetResponseRecognitionPercentage() {
        return socialNominationQuestionSetResponseRecognitionPercentage;
    }

    public void setSocialNominationQuestionSetResponseRecognitionPercentage(float socialNominationQuestionSetResponseRecognitionPercentage) {
        this.socialNominationQuestionSetResponseRecognitionPercentage = socialNominationQuestionSetResponseRecognitionPercentage;
    }

    public long getScheduleNominationQuestionSetResponseMasterChangeSeqNum() {
        return scheduleNominationQuestionSetResponseMasterChangeSeqNum;
    }

    public void setScheduleNominationQuestionSetResponseMasterChangeSeqNum(long scheduleNominationQuestionSetResponseMasterChangeSeqNum) {
        this.scheduleNominationQuestionSetResponseMasterChangeSeqNum = scheduleNominationQuestionSetResponseMasterChangeSeqNum;
    }

    public long getScheduleNominationQuestionSetResponseLocalChangeSeqNum() {
        return scheduleNominationQuestionSetResponseLocalChangeSeqNum;
    }

    public void setScheduleNominationQuestionSetResponseLocalChangeSeqNum(long scheduleNominationQuestionSetResponseLocalChangeSeqNum) {
        this.scheduleNominationQuestionSetResponseLocalChangeSeqNum = scheduleNominationQuestionSetResponseLocalChangeSeqNum;
    }

    public int getSocialNominationQuestionSetResponseLastChangedBy() {
        return socialNominationQuestionSetResponseLastChangedBy;
    }

    public void setSocialNominationQuestionSetResponseLastChangedBy(int socialNominationQuestionSetResponseLastChangedBy) {
        this.socialNominationQuestionSetResponseLastChangedBy = socialNominationQuestionSetResponseLastChangedBy;
    }
}
