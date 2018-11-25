package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SocialNominationQuestionSetResponseDao implements
        SyncableDao<SocialNominationQuestionSetResponse, SocialNominationQuestionSetResponseDao> {

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

    @UmQuery("SELECT * FROM SocialNominationQuestionSetResponse " +
            "where socialNominationQuestionSetResposeUid = :uid")
    public abstract void findByUidAsync(long uid,
                                        UmCallback<SocialNominationQuestionSetResponse> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionSetResponse WHERE " +
            "socialNominationQuestionSetResponseClazzMemberUid = :uid AND " +
            "socialNominationQuestionSetResponseRecognitionPercentage > 0.8")
    public abstract void findAllPassedRecognitionByPersonUid(long uid,
                                    UmCallback<List<SocialNominationQuestionSetResponse>> result);

    @UmQuery("SELECT Person.*   from " +
            "ClazzMember INNER JOIN PERSON ON " +
            "ClazzMember.clazzMemberUid  = Person.personUid INNER join " +
            "SocialNominationQuestionSetResponse ON  " +
            "ClazzMember.clazzMemberUid = " +
            "SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseClazzMemberUid " +
            "WHERE " +
            "SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseFinishTime > 0 " +
            "AND SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseSocialNominationQuestionSetUid > 0")
    public abstract UmProvider<Person> findAllDoneSN();
}
