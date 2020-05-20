package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption
import com.ustadmobile.lib.db.entities.SelQuestionOption

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class ClazzWorkQuestionOptionDao : BaseDao<ClazzWorkQuestionOption> {

    @Insert
    abstract override fun insert(entity: ClazzWorkQuestionOption): Long

    @Update
    abstract override fun update(entity: ClazzWorkQuestionOption)

    @Update
    abstract suspend fun updateAsync(entity: ClazzWorkQuestionOption) : Int


    suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByQuestionOptionUid(it, false)
        }
    }

    @Query("UPDATE ClazzWorkQuestionOption SET clazzWorkQuestionOptionActive = :active " +
            " WHERE clazzWorkQuestionOptionUid = :clazzWorkQuestionOptionUid ")
    abstract suspend fun updateActiveByQuestionOptionUid(clazzWorkQuestionOptionUid: Long, active : Boolean)

}