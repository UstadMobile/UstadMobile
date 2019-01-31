package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SocialNominationQuestionSetDao implements
        SyncableDao<SocialNominationQuestionSet, SocialNominationQuestionSetDao> {

    @UmInsert
    public abstract long insert(SocialNominationQuestionSet entity);

    @UmUpdate
    public abstract void update(SocialNominationQuestionSet entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestionSet entity, UmCallback<Long> result);

    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestionSet entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionSet WHERE socialNominationQuestionSetUid = :uid")
    public abstract SocialNominationQuestionSet findByUid(long uid);

    @UmQuery("SELECT * FROM SocialNominationQuestionSet WHERE socialNominationQuestionSetUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<SocialNominationQuestionSet > resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestionSet")
    public abstract UmProvider<SocialNominationQuestionSet> findAllQuestions();

    @UmQuery("SELECT " +
            " (SELECT COUNT(*) FROM SocialNominationQuestion " +
            "       WHERE socialNominationQuestionSocialNominationQuestionSetUid = " +
            "       SocialNominationQuestionSet.socialNominationQuestionSetUid) AS numQuestions, " +
            " SocialNominationQuestionSet.* " +
            "FROM SocialNominationQuestionSet ")
    public abstract UmProvider<SELQuestionSetWithNumQuestions> findAllQuestionSetsWithNumQuestions();

    @UmQuery("SELECT * FROM SocialNominationQuestionSet")
    public abstract UmLiveData<List<SocialNominationQuestionSet>> findAllQuestionSetsLiveData();

    @UmQuery("SELECT * FROM SocialNominationQuestionSet")
    public abstract void findAllQuestionsAsync(
            UmCallback<List<SocialNominationQuestionSet>> results);



}
