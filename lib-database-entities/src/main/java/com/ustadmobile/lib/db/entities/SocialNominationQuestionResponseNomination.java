package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents the student who is nominated for a specific question in the question set
 */
@UmEntity
public class SocialNominationQuestionResponseNomination {

    @UmPrimaryKey(autoIncrement = true)
    private long socialNominationQuestionResponseNominationUid;

    private long socialNominationQuestionResponseNominationClazzMemberUid;

    //<-> SocialNominationQuestionResponse entity.
    private long socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId;
}
