package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class SelQuestionSetResponseDao implements
        SyncableDao<SelQuestionSetResponse, SelQuestionSetResponseDao> {

    @UmInsert
    public abstract long insert(SelQuestionSetResponse entity);

    @UmUpdate
    public abstract void update(SelQuestionSetResponse entity);

    @UmInsert
    public abstract void insertAsync(SelQuestionSetResponse entity,
                                     UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM SelQuestionSetResponse")
    public abstract UmProvider<SelQuestionSetResponse> findAllQuestionSetResponses();

    @UmUpdate
    public abstract void updateAsync(SelQuestionSetResponse entity,
                                     UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM SelQuestionSetResponse " +
            "where selQuestionSetResposeUid = :uid")
    public abstract SelQuestionSetResponse findByUid(long uid);

    @UmQuery("SELECT * FROM SelQuestionSetResponse " +
            "where selQuestionSetResposeUid = :uid")
    public abstract void findByUidAsync(long uid,
                                        UmCallback<SelQuestionSetResponse> resultObject);

    @UmQuery("SELECT * FROM SelQuestionSetResponse WHERE " +
            "selQuestionSetResponseClazzMemberUid = :uid AND " +
            "selQuestionSetResponseRecognitionPercentage > 0.8")
    public abstract void findAllPassedRecognitionByPersonUid(long uid,
                                    UmCallback<List<SelQuestionSetResponse>> resultList);

    @UmQuery("SELECT Person.*   from " +
            "ClazzMember INNER JOIN PERSON ON " +
            "ClazzMember.clazzMemberPersonUid  = Person.personUid INNER join " +
            "SelQuestionSetResponse ON  " +
            "ClazzMember.clazzMemberUid = " +
            "SelQuestionSetResponse.selQuestionSetResponseClazzMemberUid " +
            "WHERE " +
            "SelQuestionSetResponse.selQuestionSetResponseFinishTime > 0 " +
            "AND SelQuestionSetResponse.selQuestionSetResponseSelQuestionSetUid != 0")
    public abstract UmProvider<Person> findAllDoneSN();

    @UmQuery("SELECT  " +
            "   Person.* " +
            " FROM " +
            "   ClazzMember " +
            " INNER JOIN " +
            "   PERSON ON " +
            "   ClazzMember.clazzMemberPersonUid  = Person.personUid " +
            " INNER JOIN " +
            "   SelQuestionSetResponse ON " +
            "   ClazzMember.clazzMemberUid = " +
            "   SelQuestionSetResponse.selQuestionSetResponseClazzMemberUid " +
            " WHERE" +
            "   SelQuestionSetResponse.selQuestionSetResponseFinishTime > 0 " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseSelQuestionSetUid != 0 " +
            "   AND ClazzMember.clazzMemberClazzUid = :clazzUid")
    public abstract UmProvider<Person> findAllDoneSNByClazzUid(long clazzUid);

}
