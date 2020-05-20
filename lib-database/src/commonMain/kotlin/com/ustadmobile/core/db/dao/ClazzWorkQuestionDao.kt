package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionRow

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class ClazzWorkQuestionDao : BaseDao<ClazzWorkQuestion>, OneToManyJoinDao<ClazzWorkQuestion> {

    @Query("SELECT * FROM ClazzWorkQuestion WHERE clazzWorkQuestionUid = :uid")
    abstract fun findByUid(uid: Long): ClazzWorkQuestion?

    @Query("SELECT * FROM ClazzWorkQuestion WHERE clazzWorkQuestionUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : ClazzWorkQuestion?

    @Query("SELECT coalesce(MAX(clazzWorkQuestionIndex), 0) FROM ClazzWorkQuestion WHERE " +
            "selQuestionSelQuestionSetUid = :questionSetUid " +
            " AND clazzWorkQuestionActive = 0")
    abstract suspend fun getMaxIndexByClazzWorkAsync(clazzWorkUid: Long): Int

    @Query("SELECT * FROM ClazzWorkQuestion WHERE " +
            "clazzWorkQuestionClazzWorkUid = :clazzWorkUid AND " +
            " CAST(clazzWorkQuestionActive AS INTEGER) = 0")
    abstract fun findAllActiveQuestionsInClazzWorkAsList(clazzWorkUid: Long): List<ClazzWorkQuestion>

    @Query("UPDATE ClazzWorkQuestion SET clazzWorkQuestionActive = :active " +
            "WHERE clazzWorkQuestionUid = :clazzWorkQuestionUid ")
    abstract suspend fun updateActiveByClazzWorkQuestionUid(clazzWorkQuestionUid: Long, active : Boolean)

    @Query("""
        SELECT SelQuestion.* , SelQuestionOption.* FROM SelQuestion 
        LEFT JOIN SelQuestionOption ON 
            SelQuestionOption.selQuestionOptionQuestionUid = SelQuestion.selQuestionUid 
        WHERE 
        selQuestionSelQuestionSetUid = :questionSetUid AND 
        CAST(questionActive AS INTEGER) = 1
    """)
    abstract fun findAllActiveQuestionsWithOptionsInClazzWorkAsList(clazzWorkUid: Long)
                    : List<ClazzWorkQuestionAndOptionRow>

    companion object {

        val SEL_QUESTION_TYPE_NOMINATION = 0
        val SEL_QUESTION_TYPE_MULTI_CHOICE = 1
        val SEL_QUESTION_TYPE_FREE_TEXT = 2
    }

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByClazzWorkQuestionUid(it, false)
        }
    }


}
