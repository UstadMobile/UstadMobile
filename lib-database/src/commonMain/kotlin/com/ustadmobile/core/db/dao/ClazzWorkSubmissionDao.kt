package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption
import com.ustadmobile.lib.db.entities.ClazzWorkSubmission

@UmDao
@UmRepository
@Dao
abstract class ClazzWorkSubmissionDao : BaseDao<ClazzWorkSubmission> {

    @Query("SELECT * FROM ClazzWorkSubmission WHERE clazzWorkSubmissionUid = :clazzWorkSubmissionUid " +
            " AND CAST(clazzWorkSubmissionInactive AS INTEGER) = 0")
    abstract fun findByUidAsync(clazzWorkSubmissionUid: Long): ClazzWorkSubmission?

    @Query(FIND_BY_CLAZZWORKUID)
    abstract suspend fun findByClazzWorkUidAsync(clazzWorkUid: Long): List<ClazzWorkSubmission>

    @Update
    abstract suspend fun updateAsync(entity: ClazzWorkSubmission) : Int


    companion object{
        const val FIND_BY_CLAZZWORKUID = """
            SELECT * FROM ClazzWorkSubmission WHERE clazzWorkSubmissionClazzWorkUid = :clazzWorkUid
            AND CAST(clazzWorkSubmissionInactive AS INTEGER) = 0
        """
    }
}
