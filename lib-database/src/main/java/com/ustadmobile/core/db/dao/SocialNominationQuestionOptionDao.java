package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionOption;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SocialNominationQuestionOptionDao  implements
        SyncableDao<SocialNominationQuestionOption, SocialNominationQuestionOptionDao> {

    @UmInsert
    public abstract long insert(SocialNominationQuestionOption entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestionOption entity,
                                     UmCallback<Long> resultObject);

    @UmUpdate
    public abstract void update(SocialNominationQuestionOption entity);

    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestionOption entity,
                                     UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestionOption " +
            " WHERE socialNominationQuestionOptionUid = :uid")
    public abstract SocialNominationQuestionOption findByUid(long uid);

    @UmQuery("SELECT * FROM SocialNominationQuestionOption " +
            " WHERE socialNominationQuestionOptionUid = :uid")
    public abstract void findByUidAsync(long uid,
                                        UmCallback<SocialNominationQuestionOption> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestionOption " +
            " WHERE selQuestionOptionQuestionUid = :questionUid")
    public abstract void findAllOptionsByQuestionUid(long questionUid,
                                     UmCallback<List<SocialNominationQuestionOption>> resultList);

    @UmQuery("SELECT * FROM SocialNominationQuestionOption " +
            " WHERE selQuestionOptionQuestionUid = :questionUid")
    public abstract UmProvider<SocialNominationQuestionOption>
                                            findAllOptionsByQuestionUidProvider(long questionUid);

    @UmQuery("SELECT * FROM SocialNominationQuestionOption " +
            " WHERE selQuestionOptionQuestionUid = :questionUid AND optionActive = 1")
    public abstract UmProvider<SocialNominationQuestionOption>
                                        findAllActiveOptionsByQuestionUidProvider(long questionUid);
}