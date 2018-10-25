package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponse;

@UmDao
public abstract class SocialNominationQuestionResponseDao implements BaseDao<SocialNominationQuestionResponse> {

    @UmInsert
    public abstract long insert(SocialNominationQuestionResponse entity);

    @UmUpdate
    public abstract void update(SocialNominationQuestionResponse entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestionResponse entity, UmCallback<Long> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionResponse")
    public abstract UmProvider<SocialNominationQuestionResponse> findAllQuestions();

    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestionResponse entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionResponse WHERE socialNominationQuestionResponseUid = :uid")
    public abstract SocialNominationQuestionResponse findByUid(long uid);


}
