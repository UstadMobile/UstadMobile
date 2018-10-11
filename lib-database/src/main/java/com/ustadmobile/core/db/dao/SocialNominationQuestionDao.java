package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;

@UmDao
public abstract class SocialNominationQuestionDao implements BaseDao<SocialNominationQuestion> {

    @UmInsert
    public abstract long insert(SocialNominationQuestion entity);

    @UmUpdate
    public abstract void update(SocialNominationQuestion entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestion entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM SocialNominationQuestion")
    public abstract UmProvider<SocialNominationQuestion> findAllQuestions();


    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestion entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE socialNominationQuestionUid = :uid")
    public abstract SocialNominationQuestion findByUid(long uid);


    @UmQuery("SELECT MAX(questionIndex) FROM SocialNominationQuestion;")
    public abstract void getMaxIndexAsync(UmCallback<Integer> result);


}
