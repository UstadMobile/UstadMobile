package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a set of social nomination question eg: "Question set for Region A"
 */
@UmEntity
public class SocialNominationQuestionSet  {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionSetUid;

    private String title;

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
}
