package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the question within a question set.
 * eg: "Select the students who sit alone"
 *
 */
@UmEntity
public class SocialNominationQuestion {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionUid;

    private String questionText;

    private long socialNominationQuestionSocialNominationQuestionSetUid;

    private int index;

    public long getSocialNominationQuestionUid() {
        return socialNominationQuestionUid;
    }

    public void setSocialNominationQuestionUid(long socialNominationQuestionUid) {
        this.socialNominationQuestionUid = socialNominationQuestionUid;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public long getSocialNominationQuestionSocialNominationQuestionSetUid() {
        return socialNominationQuestionSocialNominationQuestionSetUid;
    }

    public void setSocialNominationQuestionSocialNominationQuestionSetUid(long socialNominationQuestionSocialNominationQuestionSetUid) {
        this.socialNominationQuestionSocialNominationQuestionSetUid = socialNominationQuestionSocialNominationQuestionSetUid;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
