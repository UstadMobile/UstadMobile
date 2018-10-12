package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;

import java.util.List;

@UmDao
public abstract class SocialNominationQuestionSetResponseDao {

    @UmInsert
    public abstract long insert(SocialNominationQuestionSetResponse entity);

    @UmUpdate
    public abstract void update(SocialNominationQuestionSetResponse entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestionSetResponse entity,
                                     UmCallback<Long> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionSetResponse")
    public abstract UmProvider<SocialNominationQuestionSetResponse> findAllQuestionSetResponses();

    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestionSetResponse entity,
                                     UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionSetResponse " +
            "where socialNominationQuestionSetResposeUid = :uid")
    public abstract SocialNominationQuestionSetResponse findByUid(long uid);

    @UmQuery("SELECT * FROM SocialNominationQuestionSetResponse WHERE " +
            "socialNominationQuestionSetResponseClazzMemberUid = :uid AND " +
            "socialNominationQuestionSetResponseRecognitionPercentage > 0.8")
    public abstract void findAllPassedRecognitionByPersonUid(long uid,
                                    UmCallback<List<SocialNominationQuestionSetResponse>> result);
}
