package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SELNominationItem;
import com.ustadmobile.lib.db.entities.SelQuestionResponseNomination;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SelQuestionResponseNominationDao
        implements SyncableDao<SelQuestionResponseNomination,
        SelQuestionResponseNominationDao> {

    public static final String SEL_REPORT_SELECT = "SELECT " +
            "    Clazz.clazzName, " +
            "    SelQuestionSet.title AS questionSetTitle,  " +
            "    Person.firstNames || ' ' || Person.lastName AS nominatorName , " +
            "    PersonNominated.firstNames || ' ' || PersonNominated.lastName AS nomineeName, " +
            "    SelQuestion.questionText, " +
            "    ClazzMemberNominated.clazzMemberUid AS nomineeUid, " +
            "    ClazzMember.clazzMemberUid AS nominatorUid, " +
            "    Clazz.clazzUid AS clazzUid, " +
            "    SelQuestion.selQuestionUid " +
            " FROM " +
            "   SelQuestionResponseNomination " +

            " LEFT JOIN SelQuestionResponse ON " +
            "   SelQuestionResponse.selQuestionResponseUid = " +
            "   selQuestionResponseNominationSelQuestionResponseUid " +

            " LEFT JOIN SelQuestion ON " +
            "   SelQuestion.selQuestionUid = " +
            "   SelQuestionResponse.selQuestionResponseSelQuestionUid" +

            " LEFT JOIN SelQuestionSetResponse ON " +
            "   SelQuestionSetResponse.selQuestionSetResposeUid = " +
            "   SelQuestionResponse.selQuestionResponseSelQuestionSetResponseUid " +

            " LEFT JOIN SelQuestionSet ON " +
            "   SelQuestionSet.selQuestionSetUid = " +
            "   SelQuestionSetResponse.selQuestionSetResponseSelQuestionSetUid " +

            " LEFT JOIN ClazzMember ON " +
            "   ClazzMember.clazzMemberUid = " +
            "   SelQuestionSetResponse.selQuestionSetResponseClazzMemberUid " +

            " LEFT JOIN Clazz ON " +
            "   Clazz.clazzUid = ClazzMember.clazzMemberClazzUid " +

            " LEFT JOIN Person ON " +
            "   Person.personUid = ClazzMember.clazzMemberPersonUid " +

            " LEFT JOIN ClazzMember as ClazzMemberNominated ON " +
            "   ClazzMemberNominated.clazzMemberUid = " +
            "   SelQuestionResponseNomination.selQuestionResponseNominationClazzMemberUid " +

            " LEFT JOIN Person as PersonNominated ON " +
            "   PersonNominated.personUid = ClazzMemberNominated.clazzMemberPersonUid " ;

    @UmInsert
    public abstract long insert(SelQuestionResponseNomination entity);

    @UmQuery("SELECT * FROM SelQuestionResponseNomination " +
            "WHERE " +
            "selQuestionResponseNominationClazzMemberUid = :clazzMemberUid " +
            "AND selQuestionResponseNominationSelQuestionResponseUId = :questionResponseUid")
    public abstract void findExistingNomination(long clazzMemberUid, long questionResponseUid,
                            UmCallback<List<SelQuestionResponseNomination>> resultList);

    @UmUpdate
    public abstract void update(SelQuestionResponseNomination entity);

    @UmInsert
    public abstract void insertAsync(SelQuestionResponseNomination entity,
                                     UmCallback<Long> result);

    @UmQuery("SELECT * FROM SelQuestionResponseNomination")
    public abstract UmProvider<SelQuestionResponseNomination> findAllQuestions();

    @UmUpdate
    public abstract void updateAsync(SelQuestionResponseNomination entity,
                                     UmCallback<Integer> result);


    @UmQuery("SELECT * FROM SelQuestionResponseNomination " +
            "WHERE selQuestionResponseNominationUid = :uid")
    public abstract SelQuestionResponseNomination findByUid(long uid);

    @UmQuery(SEL_REPORT_SELECT +
            " WHERE " +
            "   selQuestionResponseNominationSelQuestionResponseUid != 0 " +
            "   AND nominationActive = 1 " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseStartTime > :fromTime " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseFinishTime < :toTime " +

            " ORDER BY clazzName")
    public abstract void getAllNominationsReportAllClazzesAsync(long fromTime, long toTime,
                                                 UmCallback<List<SELNominationItem>> resultList);

    @UmQuery(SEL_REPORT_SELECT +
            " WHERE " +
            "   selQuestionResponseNominationSelQuestionResponseUid != 0 " +
            "   AND nominationActive = 1 " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseStartTime > :fromTime " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseFinishTime < :toTime " +
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
