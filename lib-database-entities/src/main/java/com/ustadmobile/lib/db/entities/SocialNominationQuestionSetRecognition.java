package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

/**
 * FOR RECOGNITION
 *
 * For each run through of a question set for one student, they must attempt to recognize their
 * classmates.
 * There is 1:many relationship between this entity and SocialNominationQuestionSetResponse.
 * There is one SocialNominationQuestionSetRecognition for each
 * SocialNominationQuestionSetResponse for each student in the class..
 *
 */
@UmEntity(tableId = 26)
public class SocialNominationQuestionSetRecognition {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long socialNominationQuestionSetRecognitionUid;

    // -> SocialNominationQuestionSetResponse - The question set response (which has recognition percentages)
    private long socialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid;

    // The Clazz Member - The Student To BE recognized.
    private long socialNominationQuestionSetRecognitionClazzMemberUid;

    // Boolean if recognized or not by the ClazzMember doing this QuestionSet in QuestionSetResponse.
    private boolean socialNominationQuestionSetRecognitionRecognized;

    @UmSyncMasterChangeSeqNum
    private long scheduleNominationQuestionSetRecognitionMasterChangeSeqNum;

    @UmSyncLocalChangeSeqNum
    private long scheduleNominationQuestionSetRecognitionLocalChangeSeqNum;

    public long getSocialNominationQuestionSetRecognitionUid() {
        return socialNominationQuestionSetRecognitionUid;
    }

    public void setSocialNominationQuestionSetRecognitionUid(long socialNominationQuestionSetRecognitionUid) {
        this.socialNominationQuestionSetRecognitionUid = socialNominationQuestionSetRecognitionUid;
    }

    public long getSocialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid() {
        return socialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid;
    }

    public void setSocialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid(long socialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid) {
        this.socialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid = socialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid;
    }

    public long getSocialNominationQuestionSetRecognitionClazzMemberUid() {
        return socialNominationQuestionSetRecognitionClazzMemberUid;
    }

    public void setSocialNominationQuestionSetRecognitionClazzMemberUid(long socialNominationQuestionSetRecognitionClazzMemberUid) {
        this.socialNominationQuestionSetRecognitionClazzMemberUid = socialNominationQuestionSetRecognitionClazzMemberUid;
    }

    public boolean isSocialNominationQuestionSetRecognitionRecognized() {
        return socialNominationQuestionSetRecognitionRecognized;
    }

    public void setSocialNominationQuestionSetRecognitionRecognized(boolean socialNominationQuestionSetRecognitionRecognized) {
        this.socialNominationQuestionSetRecognitionRecognized = socialNominationQuestionSetRecognitionRecognized;
    }

    public long getScheduleNominationQuestionSetRecognitionMasterChangeSeqNum() {
        return scheduleNominationQuestionSetRecognitionMasterChangeSeqNum;
    }

    public void setScheduleNominationQuestionSetRecognitionMasterChangeSeqNum(long scheduleNominationQuestionSetRecognitionMasterChangeSeqNum) {
        this.scheduleNominationQuestionSetRecognitionMasterChangeSeqNum = scheduleNominationQuestionSetRecognitionMasterChangeSeqNum;
    }

    public long getScheduleNominationQuestionSetRecognitionLocalChangeSeqNum() {
        return scheduleNominationQuestionSetRecognitionLocalChangeSeqNum;
    }

    public void setScheduleNominationQuestionSetRecognitionLocalChangeSeqNum(long scheduleNominationQuestionSetRecognitionLocalChangeSeqNum) {
        this.scheduleNominationQuestionSetRecognitionLocalChangeSeqNum = scheduleNominationQuestionSetRecognitionLocalChangeSeqNum;
    }
}
