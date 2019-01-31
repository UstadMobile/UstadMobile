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

    public static final String SEL_REPORT_SELECT = "SELECT " +
            "    Clazz.clazzName, " +
            "    SocialNominationQuestionSet.title AS questionSetTitle,  " +
            "    Person.firstNames || ' ' || Person.lastName AS nominatorName , " +
            "    PersonNominated.firstNames || ' ' || PersonNominated.lastName AS nomineeName, " +
            "    SocialNominationQuestion.questionText, " +
            "    ClazzMemberNominated.clazzMemberUid AS nomineeUid, " +
            "    ClazzMember.clazzMemberUid AS nominatorUid, " +
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
            "   PersonNominated.personUid = ClazzMemberNominated.clazzMemberPersonUid " ;

    @UmInsert
    public abstract long insert(SocialNominationQuestionResponseNomination entity);

    @UmQuery("SELECT * FROM SocialNominationQuestionResponseNomination " +
            "WHERE " +
            "socialNominationQuestionResponseNominationClazzMemberUid = :clazzMemberUid " +
            "AND socialNominationQuestionResponseNominationSocialNominationQuestionResponseUId = :questionResponseUid")
    public abstract void findExistingNomination(long clazzMemberUid, long questionResponseUid,
                            UmCallback<List<SocialNominationQuestionResponseNomination>> resultList);

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

    @UmQuery(SEL_REPORT_SELECT +
            " WHERE " +
            "   socialNominationQuestionResponseNominationSocialNominationQuestionResponseUid != 0 " +
            "   AND nominationActive = 1 " +
            "   AND SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseStartTime > :fromTime " +
            "   AND SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseFinishTime < :toTime " +

            " ORDER BY clazzName")
    public abstract void getAllNominationsReportAllClazzesAsync(long fromTime, long toTime,
                                                 UmCallback<List<SELNominationItem>> resultList);

    @UmQuery(SEL_REPORT_SELECT +
            " WHERE " +
            "   socialNominationQuestionResponseNominationSocialNominationQuestionResponseUid != 0 " +
            "   AND nominationActive = 1 " +
            "   AND SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseStartTime > :fromTime " +
            "   AND SocialNominationQuestionSetResponse.socialNominationQuestionSetResponseFinishTime < :toTime " +
            "   AND Clazz.clazzUid IN (:clazzList) " +
            " ORDER BY clazzName")
    public abstract void getAllNominationsReportInClazzAsync(long fromTime, long toTime, List<Long> clazzList,
                                                        UmCallback<List<SELNominationItem>> resultList);

    public void getAllNominationReportAsync(long fromTime, long toTime, List<Long> clazzes,
                                            UmCallback<List<SELNominationItem>> resultList){
        if(clazzes != null && !clazzes.isEmpty()){
            getAllNominationsReportInClazzAsync(fromTime, toTime, clazzes, resultList);
        }else{
            getAllNominationsReportAllClazzesAsync(fromTime, toTime, resultList);
        }
    }


}
