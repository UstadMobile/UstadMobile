package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SELNominationItem;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponseNomination;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

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

    @UmQuery("SELECT " +
            "    Clazz.clazzName, " +
            "    SocialNominationQuestionSet.title AS questionSetTitle,  " +
            "    Person.firstNames || ' ' || Person.lastName AS nominatorName , " +
            "    PersonNominated.firstNames || ' ' || PersonNominated.lastName AS nomineeName, " +
            "    SocialNominationQuestion.questionText, " +
            "    PersonNominated.personUid AS nomineeUid, " +
            "    Person.personUid AS nominatorUid, " +
            "    Clazz.clazzUid AS clazzUid, " +
            "    SocialNominationQuestion.socialNominationQuestionUid " +
            " FROM " +
            "   SocialNominationQuestionResponseNomination " +

            " LEFT JOIN SocialNominationQuestionResponse ON " +
            "   SocialNominationQuestionResponse.socialNominationQuestionResponseUid = " +
            "   socialNominationQuestionResponseNominationSocialNominationQuestionResponseUid " +

            " LEFT JOIN SocialNominationQuestion ON " +
            "   SocialNominationQuestion.socialNominationQuestionUid = " +
            "   SocialNominationQuestionResponse.socialNominationQuestionResponseSocialNominationQuestionUid" +

            " LEFT JOIN SocialNominationQuestionSetResponse ON " +
            "   SocialNominationQuestionSetResponse.socialNominationQuestionSetResposeUid = " +
            "   SocialNominationQuestionResponse.socialNominationQuestionResponseSocialNominationQuestionSetResponseUid " +

            " LEFT JOIN SocialNominationQuestionSet ON " +
            "   SocialNominationQuestionSet.socialNominationQuestionSetUid = " +
            "   SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseSocialNominationQuestionSetUid " +

            " LEFT JOIN ClazzMember ON " +
            "   ClazzMember.clazzMemberUid = " +
            "   SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseClazzMemberUid " +

            " LEFT JOIN Clazz ON " +
            "   Clazz.clazzUid = ClazzMember.clazzMemberClazzUid " +

            " LEFT JOIN Person ON " +
            "   Person.personUid = ClazzMember.clazzMemberPersonUid " +

            " LEFT JOIN ClazzMember as ClazzMemberNominated ON " +
            "   ClazzMemberNominated.clazzMemberUid = " +
            "   SocialNominationQuestionResponseNomination.socialNominationQuestionResponseNominationClazzMemberUid " +

            " LEFT JOIN Person as PersonNominated ON " +
            "   PersonNominated.personUid = ClazzMemberNominated.clazzMemberPersonUid " +
            " WHERE " +
            "   socialNominationQuestionResponseNominationSocialNominationQuestionResponseUid != 0 " +
            " ORDER BY clazzName")
    public abstract void getAllNominationsReport(UmCallback<List<SELNominationItem>> resultList);


}
