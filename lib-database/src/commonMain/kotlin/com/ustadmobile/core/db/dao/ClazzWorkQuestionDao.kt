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


    @Query("UPDATE ClazzWorkQuestion SET clazzWorkQuestionActive = :active " +
            "WHERE clazzWorkQuestionUid = :clazzWorkQuestionUid ")
    abstract suspend fun updateActiveByClazzWorkQuestionUid(clazzWorkQuestionUid: Long, active : Boolean)

    @Query("""
        SELECT ClazzWorkQuestion.* , ClazzWorkQuestionOption.* FROM ClazzWorkQuestion 
        LEFT JOIN ClazzWorkQuestionOption ON 
            ClazzWorkQuestionOption.clazzWorkQuestionOptionQuestionUid = ClazzWorkQuestion.clazzWorkQuestionUid AND CAST(ClazzWorkQuestionOption.clazzWorkQuestionOptionActive AS INTEGER) = 1
        WHERE 
        ClazzWorkQuestion.clazzWorkQuestionClazzWorkUid = :clazzWorkUid 
        AND CAST(ClazzWorkQuestion.clazzWorkQuestionActive AS INTEGER) = 1	
    """)
    abstract suspend fun findAllActiveQuestionsWithOptionsInClazzWorkAsList(clazzWorkUid: Long)
                    : List<ClazzWorkQuestionAndOptionRow>

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByClazzWorkQuestionUid(it, false)
        }
    }


}
