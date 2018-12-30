package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponseNomination;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SocialNominationQuestionResponseNominationDao
        implements SyncableDao<SocialNominationQuestionResponseNomination,
        SocialNominationQuestionResponseNominationDao> {

    @UmInsert
    public abstract long insert(SocialNominationQuestionResponseNomination entity);

    @UmUpdate
    public abstract void update(SocialNominationQuestionResponseNomination entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestionResponseNomination entity,
                                     UmCallback<Long> result);

    @UmQuery("SELECT * FROM SocialNominationQuestionResponseNomination")
    public abstract UmProvider<SocialNominationQuestionResponseNomination> findAllQuestions();

    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestionResponseNomination entity,
                                     UmCallback<Integer> result);


    @UmQuery("SELECT * FROM SocialNominationQuestionResponseNomination " +
            "WHERE socialNominationQuestionResponseNominationUid = :uid")
    public abstract SocialNominationQuestionResponseNomination findByUid(long uid);



}
