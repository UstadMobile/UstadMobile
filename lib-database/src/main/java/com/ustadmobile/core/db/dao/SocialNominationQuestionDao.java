package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;

import java.util.List;

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

    @UmQuery("SELECT MAX(questionIndex) FROM SocialNominationQuestion")
    public abstract void getMaxIndexAsync(UmCallback<Integer> result);

    @UmQuery("SELECT * FROM SocialNominationQuestion where " +
            "socialNominationQuestionSocialNominationQuestionSetUid = :questionSetUid")
    public abstract void findAllByQuestionSetUidAsync(long questionSetUid,
                                          UmCallback<List<SocialNominationQuestion>> result);

    @UmQuery("SELECT * FROM SocialNominationQuestion WHERE " +
            " socialNominationQuestionSocialNominationQuestionSetUid = :questionSetUid " +
            " AND questionIndex > :previousIndex ORDER BY questionIndex ASC LIMIT 1    " )
    public abstract void findNextQuestionByQuestionSetUidAsync(long questionSetUid,
                               int previousIndex, UmCallback<SocialNominationQuestion> snQuestion);

    @UmQuery("SELECT MIN(questionIndex) FROM SocialNominationQuestion")
    public abstract void getMinIndexAsync(UmCallback<Integer> result);

    @UmQuery("SELECT COUNT(*) FROM SocialNominationQuestion")
    public abstract int findTotalNumberOfQuestions();



}
