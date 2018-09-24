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

    //
    private long socialNominationQuestionSetResponseSocialNominationQuestionSetUid;

    //clazz member doing this
    private long socialNominationQuestionSetResponseClazzMemberUid;

    //start tiem
    private long socialNominationQuestionSetResponseStartTime;

    //finish time
    private long socialNominationQuestionSetResponseFinishTime;

    //total Response percentage
    private float socialNominationQuestionSetResponseRecognitionPercentage;
}
