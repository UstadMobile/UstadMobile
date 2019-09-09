package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions
import com.ustadmobile.lib.db.entities.SelQuestionSet

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Dao
abstract class SelQuestionSetDao : BaseDao<SelQuestionSet> {

    @Insert
    abstract override fun insert(entity: SelQuestionSet): Long

    @Update
    abstract override fun update(entity: SelQuestionSet)

    @Update
    abstract suspend fun updateAsync(entity: SelQuestionSet) : Int

    @Query("SELECT * FROM SelQuestionSet WHERE selQuestionSetUid = :uid")
    abstract fun findByUid(uid: Long): SelQuestionSet?

    @Query("SELECT * FROM SelQuestionSet WHERE selQuestionSetUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : SelQuestionSet?

    @Query("SELECT * FROM SelQuestionSet")
    abstract fun findAllQuestions(): DataSource.Factory<Int, SelQuestionSet>

    @Query("SELECT " +
            " (SELECT COUNT(*) FROM SelQuestion " +
            "       WHERE selQuestionSelQuestionSetUid = " +
            "       SelQuestionSet.selQuestionSetUid) AS numQuestions, " +
            " SelQuestionSet.* " +
            "FROM SelQuestionSet ")
    abstract fun findAllQuestionSetsWithNumQuestions(): DataSource.Factory<Int, SELQuestionSetWithNumQuestions>

    @Query("SELECT * FROM SelQuestionSet")
    abstract fun findAllQuestionSetsLiveData(): DoorLiveData<List<SelQuestionSet>>

    @Query("SELECT * FROM SelQuestionSet")
    abstract suspend fun findAllQuestionsAsync(): List<SelQuestionSet>


}
