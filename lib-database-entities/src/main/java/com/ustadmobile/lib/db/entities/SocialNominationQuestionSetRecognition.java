package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * For each run through of a question set for one student, they must attempt to recognize their
 * classmates.
 * There is 1:many relationship between this entity and SocialNominationQuestionSetResponse.
 * There is one SocialNominationQuestinSetRecognition for each
 * SocialNominationQuestionSetResponse for each student in the class..
 *
 */
@UmEntity
public class SocialNominationQuestionSetRecognition {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionSetRecognitionUid;

    private long socialNominationQuestionSetRecognitionSocialNominationQuestionSetResponseUid;

    private long socialNominationQuestionSetRecognitionClazzMemberUid;

    private boolean socialNominationQuestionSetRecognitionRecognized;

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
}
