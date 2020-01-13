package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SelQuestionResponse

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class SelQuestionResponseDao : BaseDao<SelQuestionResponse> {

    @Insert
    abstract override fun insert(entity: SelQuestionResponse): Long

    @Update
    abstract override fun update(entity: SelQuestionResponse)

    @Query("SELECT * FROM SelQuestionResponse")
    abstract fun findAllQuestions(): DataSource.Factory<Int, SelQuestionResponse>

    @Update
    abstract suspend fun updateAsync(entity: SelQuestionResponse) : Int 

    @Query("SELECT * FROM SelQuestionResponse " + "WHERE selQuestionResponseUid = :uid")
    abstract fun findByUid(uid: Long): SelQuestionResponse?


}
