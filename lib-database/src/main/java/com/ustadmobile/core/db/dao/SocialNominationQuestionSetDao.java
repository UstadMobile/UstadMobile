package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
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

    @UmQuery("SELECT * FROM SocialNominationQuestionSet")
    public abstract UmProvider<SocialNominationQuestionSet> findAllQuestions();

    @UmQuery("SELECT * FROM SocialNominationQuestionSet")
    public abstract void findAllQuestionsAsync(
            UmCallback<List<SocialNominationQuestionSet>> results);



}
