package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the students response to a specific question in the question set
 */
@UmEntity
public class SocialNominationQuestionResponse {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionResponseUid;

    private long socialNominationQuestionResponseSocialNominationQuestionSetResponseUid;


}
