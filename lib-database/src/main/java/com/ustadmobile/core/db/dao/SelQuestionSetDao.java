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
import com.ustadmobile.lib.db.entities.SelQuestionSet;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SelQuestionSetDao implements
        SyncableDao<SelQuestionSet, SelQuestionSetDao> {

    @UmInsert
    public abstract long insert(SelQuestionSet entity);

    @UmUpdate
    public abstract void update(SelQuestionSet entity);

    @UmInsert
    public abstract void insertAsync(SelQuestionSet entity, UmCallback<Long> result);

    @UmUpdate
    public abstract void updateAsync(SelQuestionSet entity, UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SelQuestionSet WHERE selQuestionSetUid = :uid")
    public abstract SelQuestionSet findByUid(long uid);

    @UmQuery("SELECT * FROM SelQuestionSet WHERE selQuestionSetUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<SelQuestionSet> resultObject);

    @UmQuery("SELECT * FROM SelQuestionSet")
    public abstract UmProvider<SelQuestionSet> findAllQuestions();

    @UmQuery("SELECT " +
            " (SELECT COUNT(*) FROM SelQuestion " +
            "       WHERE selQuestionSelQuestionSetUid = " +
            "       SelQuestionSet.selQuestionSetUid) AS numQuestions, " +
            " SelQuestionSet.* " +
            "FROM SelQuestionSet ")
    public abstract UmProvider<SELQuestionSetWithNumQuestions> findAllQuestionSetsWithNumQuestions();

    @UmQuery("SELECT * FROM SelQuestionSet")
    public abstract UmLiveData<List<SelQuestionSet>> findAllQuestionSetsLiveData();

    @UmQuery("SELECT * FROM SelQuestionSet")
    public abstract void findAllQuestionsAsync(
            UmCallback<List<SelQuestionSet>> results);



}
