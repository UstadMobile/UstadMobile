package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class SelQuestionSetResponseDao : BaseDao<SelQuestionSetResponse> {

    @Insert
    abstract override fun insert(entity: SelQuestionSetResponse): Long

    @Update
    abstract override fun update(entity: SelQuestionSetResponse)

    @Insert
    abstract fun insertAsync(entity: SelQuestionSetResponse,
                             resultObject: UmCallback<Long>)

    @Query("SELECT * FROM SelQuestionSetResponse")
    abstract fun findAllQuestionSetResponses(): DataSource.Factory<Int, SelQuestionSetResponse>

    @Update
    abstract fun updateAsync(entity: SelQuestionSetResponse,
                             resultObject: UmCallback<Int>)

    @Query("SELECT * FROM SelQuestionSetResponse " + "where selQuestionSetResposeUid = :uid")
    abstract fun findByUid(uid: Long): SelQuestionSetResponse

    @Query("SELECT * FROM SelQuestionSetResponse " + "where selQuestionSetResposeUid = :uid")
    abstract fun findByUidAsync(uid: Long,
                                resultObject: UmCallback<SelQuestionSetResponse>)

    @Query("SELECT * FROM SelQuestionSetResponse WHERE " +
            "selQuestionSetResponseClazzMemberUid = :uid AND " +
            "selQuestionSetResponseRecognitionPercentage > 0.8")
    abstract fun findAllPassedRecognitionByPersonUid(uid: Long,
                                                     resultList: UmCallback<List<SelQuestionSetResponse>>)

    @Query("SELECT Person.*   from " +
            "ClazzMember INNER JOIN PERSON ON " +
            "ClazzMember.clazzMemberPersonUid  = Person.personUid INNER join " +
            "SelQuestionSetResponse ON  " +
            "ClazzMember.clazzMemberUid = " +
            "SelQuestionSetResponse.selQuestionSetResponseClazzMemberUid " +
            "WHERE " +
            "SelQuestionSetResponse.selQuestionSetResponseFinishTime > 0 " +
            "AND SelQuestionSetResponse.selQuestionSetResponseSelQuestionSetUid != 0")
    abstract fun findAllDoneSN(): DataSource.Factory<Int, Person>

    @Query("SELECT  " +
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
    abstract fun findAllDoneSNByClazzUid(clazzUid: Long): DataSource.Factory<Int, Person>

}
