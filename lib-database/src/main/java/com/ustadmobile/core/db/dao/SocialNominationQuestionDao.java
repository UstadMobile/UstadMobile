package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SocialNominationQuestionDao implements
        SyncableDao<SocialNominationQuestion, SocialNominationQuestionDao> {

    public static final int SEL_QUESTION_TYPE_NOMINATION = 0;
    public static final int SEL_QUESTION_TYPE_MULTI_CHOICE = 1;
    public static final int SEL_QUESTION_TYPE_FREE_TEXT = 2;

    @UmInsert
    public abstract long insert(SocialNominationQuestion entity);

    @UmUpdate
    public abstract void update(SocialNominationQuestion entity);

    @UmInsert
    public abstract void insertAsync(SocialNominationQuestion entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestion")
    public abstract UmProvider<SocialNominationQuestion> findAllQuestions();

    @UmUpdate
    public abstract void updateAsync(SocialNominationQuestion entity, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE socialNominationQuestionUid = :uid")
    public abstract SocialNominationQuestion findByUid(long uid);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE socialNominationQuestionUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<SocialNominationQuestion> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE socialNominationQuestionUid = :uid")
    public abstract UmLiveData<SocialNominationQuestion> findByUidLive(long uid);

    @UmQuery("SELECT MAX(questionIndex) FROM SocialNominationQuestion")
    public abstract void getMaxIndexAsync(UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestion where " +
            "socialNominationQuestionSocialNominationQuestionSetUid = :questionSetUid")
    public abstract void findAllByQuestionSetUidAsync(long questionSetUid,
                                          UmCallback<List<SocialNominationQuestion>> resultObject);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE " +
            "socialNominationQuestionSocialNominationQuestionSetUid = :questionUid")
    public abstract UmProvider<SocialNominationQuestion> findAllQuestionsInSet(long questionUid);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE " +
            "socialNominationQuestionSocialNominationQuestionSetUid = :questionUid AND " +
            "questionActive = 1")
    public abstract UmProvider<SocialNominationQuestion> findAllActivrQuestionsInSet(long questionUid);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE " +
            " socialNominationQuestionSocialNominationQuestionSetUid = :questionSetUid " +
            " AND questionIndex > :previousIndex ORDER BY questionIndex ASC LIMIT 1    " )
    public abstract void findNextQuestionByQuestionSetUidAsync(long questionSetUid,
                               int previousIndex, UmCallback<SocialNominationQuestion> snQuestion);

    @UmQuery("SELECT MIN(questionIndex) FROM SocialNominationQuestion")
    public abstract void getMinIndexAsync(UmCallback<Integer> resultObject);

    @UmQuery("SELECT COUNT(*) FROM SocialNominationQuestion")
    public abstract int findTotalNumberOfQuestions();

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE questionText = :question")
    public abstract void findByQuestionStringAsync(String question,
                                           UmCallback<List<SocialNominationQuestion>> resultList);



}
