package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SelQuestionDao implements
        SyncableDao<SelQuestion, SelQuestionDao> {

    public static final int SEL_QUESTION_TYPE_NOMINATION = 0;
    public static final int SEL_QUESTION_TYPE_MULTI_CHOICE = 1;
    public static final int SEL_QUESTION_TYPE_FREE_TEXT = 2;

    @UmInsert
    public abstract long insert(SelQuestion entity);

    @UmUpdate
    public abstract void update(SelQuestion entity);

    @UmInsert
    public abstract void insertAsync(SelQuestion entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM SelQuestion")
    public abstract UmProvider<SelQuestion> findAllQuestions();

    @UmUpdate
    public abstract void updateAsync(SelQuestion entity, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SelQuestion WHERE selQuestionUid = :uid")
    public abstract SelQuestion findByUid(long uid);

    @UmQuery("SELECT * FROM SelQuestion WHERE selQuestionUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<SelQuestion> resultObject);

    @UmQuery("SELECT * FROM SelQuestion WHERE selQuestionUid = :uid")
    public abstract UmLiveData<SelQuestion> findByUidLive(long uid);

    @UmQuery("SELECT MAX(questionIndex) FROM SelQuestion")
    public abstract void getMaxIndexAsync(UmCallback<Integer> resultObject);

    @UmQuery("SELECT MAX(questionIndex) FROM SelQuestion WHERE " +
            "selQuestionSelQuestionSetUid = :questionSetUid " +
            " AND questionActive = 1")
    public abstract void getMaxIndexByQuestionSetAsync(long questionSetUid,
                                                       UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SelQuestion where " +
            "selQuestionSelQuestionSetUid = :questionSetUid")
    public abstract void findAllByQuestionSetUidAsync(long questionSetUid,
                                          UmCallback<List<SelQuestion>> resultObject);

    @UmQuery("SELECT * FROM SelQuestion WHERE " +
            "selQuestionSelQuestionSetUid = :questionUid")
    public abstract UmProvider<SelQuestion> findAllQuestionsInSet(long questionUid);

    @UmQuery("SELECT * FROM SelQuestion WHERE " +
            "selQuestionSelQuestionSetUid = :questionUid AND " +
            "questionActive = 1")
    public abstract UmProvider<SelQuestion> findAllActivrQuestionsInSet(long questionUid);

    @UmQuery("SELECT * FROM SelQuestion WHERE " +
            " selQuestionSelQuestionSetUid = :questionSetUid " +
            " AND questionIndex > :previousIndex ORDER BY questionIndex ASC LIMIT 1    " )
    public abstract void findNextQuestionByQuestionSetUidAsync(long questionSetUid,
                               int previousIndex, UmCallback<SelQuestion> snQuestion);

    @UmQuery("SELECT MIN(questionIndex) FROM SelQuestion")
    public abstract void getMinIndexAsync(UmCallback<Integer> resultObject);

    @UmQuery("SELECT COUNT(*) FROM SelQuestion")
    public abstract int findTotalNumberOfQuestions();

    @UmQuery("SELECT COUNT(*) FROM SelQuestion WHERE" +
            " selQuestionSelQuestionSetUid = :questionSetUid AND " +
            " questionActive = 1")
    public abstract int findTotalNumberOfActiveQuestionsInAQuestionSet(long questionSetUid);

    @UmQuery("SELECT * FROM SelQuestion WHERE questionText = :question")
    public abstract void findByQuestionStringAsync(String question,
                                           UmCallback<List<SelQuestion>> resultList);



}
