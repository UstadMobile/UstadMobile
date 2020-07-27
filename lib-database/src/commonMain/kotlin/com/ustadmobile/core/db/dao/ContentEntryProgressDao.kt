package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ContentEntryProgressDao.Companion.FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ContentEntryProgress

@UmDao
@UmRepository
@Dao
abstract class ContentEntryProgressDao : BaseDao<ContentEntryProgress> {

    @Update
    abstract suspend fun updateAsync(contentEntryProgress: ContentEntryProgress): Int

    @Query(FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY)
    abstract fun getProgressByContentAndPerson(contentEntryUid: Long, personUid: Long): ContentEntryProgress?


    @Query("""UPDATE ContentEntryProgress SET contentEntryProgressProgress = :progress, 
                    contentEntryProgressStatusFlag = :status WHERE 
                    contentEntryProgressPersonUid = :personUid AND 
                    contentEntryProgressContentEntryUid = :contentEntryUid
                    """)
    abstract fun updateProgressByContentEntryAndPerson(contentEntryUid: Long, personUid: Long,
                                                       progress: Int, status: Int): Int

    companion object {

        const val FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY =
                """
                SELECT * FROM ContentEntryProgress WHERE 
                contentEntryProgress.contentEntryProgressContentEntryUid = :contentEntryUid
                AND contentEntryProgressPersonUid = :personUid
                AND CAST(contentEntryProgressActive AS INTEGER) = 1
                """
        const val FIND_PROGRESS_BY_CONTENT_PERSON_AND_STATUS_QUERY =
                """
                    $FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY 
                    AND contentEntryProgressStatusFlag = :statusFlag
                """
    }
}
