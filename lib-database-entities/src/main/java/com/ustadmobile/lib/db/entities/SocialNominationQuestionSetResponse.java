package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents one run through of a question set for one particular student.
 */
@UmEntity
public class SocialNominationQuestionSetResponse {

    @UmPrimaryKey(autoIncrement = true)
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
}
