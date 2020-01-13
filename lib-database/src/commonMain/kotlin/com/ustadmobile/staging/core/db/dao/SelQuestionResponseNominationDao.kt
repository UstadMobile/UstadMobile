package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SELNominationItem
import com.ustadmobile.lib.db.entities.SelQuestionResponseNomination

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class SelQuestionResponseNominationDao : BaseDao<SelQuestionResponseNomination> {

    @Insert
    abstract override fun insert(entity: SelQuestionResponseNomination): Long

    @Query("SELECT * FROM SelQuestionResponseNomination " +
            "WHERE " +
            "selqrnClazzMemberUid = :clazzMemberUid " +
            "AND selqrnSelQuestionResponseUId = :questionResponseUid")
    abstract suspend fun findExistingNomination(clazzMemberUid: Long, questionResponseUid: Long)
            : List<SelQuestionResponseNomination>

    @Update
    abstract override fun update(entity: SelQuestionResponseNomination)

    @Query("SELECT * FROM SelQuestionResponseNomination")
    abstract fun findAllQuestions(): DataSource.Factory<Int, SelQuestionResponseNomination>

    @Update
    abstract suspend fun updateAsync(entity: SelQuestionResponseNomination) : Int


    @Query("SELECT * FROM SelQuestionResponseNomination " + "WHERE selqrnUid = :uid")
    abstract fun findByUid(uid: Long): SelQuestionResponseNomination?

    @Query(SEL_REPORT_SELECT +
            " WHERE " +
            "   selqrnSelQuestionResponseUid != 0 " +
            "   AND nominationActive = 1 " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseStartTime > :fromTime " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseFinishTime < :toTime " +

            " ORDER BY clazzName")
    abstract suspend fun getAllNominationsReportAllClazzesAsync(fromTime: Long, toTime: Long) :
            List<SELNominationItem>

    @Query(SEL_REPORT_SELECT +
            " WHERE " +
            "   selqrnSelQuestionResponseUid != 0 " +
            "   AND nominationActive = 1 " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseStartTime > :fromTime " +
            "   AND SelQuestionSetResponse.selQuestionSetResponseFinishTime < :toTime " +
            "   AND Clazz.clazzUid IN (:clazzList) " +
            " ORDER BY clazzName")
    abstract suspend fun getAllNominationsReportInClazzAsync(fromTime: Long, toTime: Long, clazzList:
    List<Long>) : List<SELNominationItem>

    suspend fun getAllNominationReportAsync(fromTime: Long, toTime: Long, clazzes: List<Long>?) :
            List<SELNominationItem> {
        if (clazzes != null && !clazzes.isEmpty()) {
            return getAllNominationsReportInClazzAsync(fromTime, toTime, clazzes)
        } else {
            return getAllNominationsReportAllClazzesAsync(fromTime, toTime)
        }
    }

    companion object {

        const val SEL_REPORT_SELECT = "SELECT " +
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
                "   selqrnSelQuestionResponseUid " +

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
                "   SelQuestionResponseNomination.selqrnClazzMemberUid " +

                " LEFT JOIN Person as PersonNominated ON " +
                "   PersonNominated.personUid = ClazzMemberNominated.clazzMemberPersonUid "
    }


}
