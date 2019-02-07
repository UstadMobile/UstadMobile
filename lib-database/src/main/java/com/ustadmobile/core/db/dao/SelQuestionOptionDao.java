package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SelQuestionOption;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SelQuestionOptionDao implements
        SyncableDao<SelQuestionOption, SelQuestionOptionDao> {

    @UmInsert
    public abstract long insert(SelQuestionOption entity);

    @UmInsert
    public abstract void insertAsync(SelQuestionOption entity,
                                     UmCallback<Long> resultObject);

    @UmUpdate
    public abstract void update(SelQuestionOption entity);

    @UmUpdate
    public abstract void updateAsync(SelQuestionOption entity,
                                     UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SelQuestionOption " +
            " WHERE selQuestionOptionUid = :uid")
    public abstract SelQuestionOption findByUid(long uid);

    @UmQuery("SELECT * FROM SelQuestionOption " +
            " WHERE selQuestionOptionUid = :uid")
    public abstract void findByUidAsync(long uid,
                                        UmCallback<SelQuestionOption> resultObject);

    @UmQuery("SELECT * FROM SelQuestionOption " +
            " WHERE selQuestionOptionQuestionUid = :questionUid")
    public abstract void findAllOptionsByQuestionUid(long questionUid,
                                     UmCallback<List<SelQuestionOption>> resultList);

    @UmQuery("SELECT * FROM SelQuestionOption " +
            " WHERE selQuestionOptionQuestionUid = :questionUid")
    public abstract UmProvider<SelQuestionOption>
                                            findAllOptionsByQuestionUidProvider(long questionUid);

    @UmQuery("SELECT * FROM SelQuestionOption " +
            " WHERE selQuestionOptionQuestionUid = :questionUid AND optionActive = 1")
    public abstract UmProvider<SelQuestionOption>
                                        findAllActiveOptionsByQuestionUidProvider(long questionUid);
}