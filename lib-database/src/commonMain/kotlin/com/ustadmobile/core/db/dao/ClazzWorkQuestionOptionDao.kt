package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption

@Repository
@Dao
abstract class ClazzWorkQuestionOptionDao : BaseDao<ClazzWorkQuestionOption>,
        OneToManyJoinDao<ClazzWorkQuestionOption>{

    @Update
    abstract suspend fun updateAsync(entity: ClazzWorkQuestionOption) : Int


    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByQuestionOptionUid(it, false)
        }
    }

    @Query("UPDATE ClazzWorkQuestionOption SET clazzWorkQuestionOptionActive = :active " +
            " WHERE clazzWorkQuestionOptionUid = :clazzWorkQuestionOptionUid ")
    abstract suspend fun updateActiveByQuestionOptionUid(clazzWorkQuestionOptionUid: Long, active : Boolean)

}