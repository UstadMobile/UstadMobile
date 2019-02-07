package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SelQuestionResponse;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SelQuestionResponseDao implements
        SyncableDao<SelQuestionResponse, SelQuestionResponseDao> {

    @UmInsert
    public abstract long insert(SelQuestionResponse entity);

    @UmUpdate
    public abstract void update(SelQuestionResponse entity);

    @UmInsert
    public abstract void insertAsync(SelQuestionResponse entity,
                                     UmCallback<Long> result);

    @UmQuery("SELECT * FROM SelQuestionResponse")
    public abstract UmProvider<SelQuestionResponse> findAllQuestions();

    @UmUpdate
    public abstract void updateAsync(SelQuestionResponse entity,
                                     UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SelQuestionResponse " +
            "WHERE selQuestionResponseUid = :uid")
    public abstract SelQuestionResponse findByUid(long uid);


}
