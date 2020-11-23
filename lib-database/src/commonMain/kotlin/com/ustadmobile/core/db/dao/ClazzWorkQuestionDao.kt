package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionRow
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponseRow

@Repository
@Dao
abstract class ClazzWorkQuestionDao : BaseDao<ClazzWorkQuestion>, OneToManyJoinDao<ClazzWorkQuestion> {

    @Query("SELECT * FROM ClazzWorkQuestion WHERE clazzWorkQuestionUid = :uid")
    abstract fun findByUid(uid: Long): ClazzWorkQuestion?

    @Query("SELECT * FROM ClazzWorkQuestion WHERE clazzWorkQuestionUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : ClazzWorkQuestion?


    @Query("""UPDATE ClazzWorkQuestion SET clazzWorkQuestionActive = :active,
            clazzWorkQuestionLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE clazzWorkQuestionUid = :clazzWorkQuestionUid """)
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

    @Query("""
        SELECT ClazzWorkQuestion.* , ClazzWorkQuestionOption.*, ClazzWorkQuestionResponse.* 

        FROM ClazzWorkQuestion 
        LEFT JOIN ClazzWorkQuestionOption ON 
            ClazzWorkQuestionOption.clazzWorkQuestionOptionQuestionUid = ClazzWorkQuestion.clazzWorkQuestionUid 
            AND CAST(ClazzWorkQuestionOption.clazzWorkQuestionOptionActive AS INTEGER) = 1
        LEFT JOIN ClazzWorkQuestionResponse ON 
            ClazzWorkQuestionResponse.clazzWorkQuestionResponseQuestionUid = ClazzWorkQuestion.clazzWorkQuestionUid
            AND CAST(clazzWorkQuestionResponseInactive AS INTEGER) = 0
            AND clazzWorkQuestionResponseClazzMemberUid = :clazzMemberUid
        WHERE 
        ClazzWorkQuestion.clazzWorkQuestionClazzWorkUid = :clazzWorkUid 
        AND CAST(ClazzWorkQuestion.clazzWorkQuestionActive AS INTEGER) = 1	
    """)
    abstract suspend fun findAllQuestionsAndOptionsWithResponse(clazzWorkUid: Long,
                                                                clazzMemberUid: Long)
            :List<ClazzWorkQuestionAndOptionWithResponseRow>


    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByClazzWorkQuestionUid(it, false)
        }
    }


}
