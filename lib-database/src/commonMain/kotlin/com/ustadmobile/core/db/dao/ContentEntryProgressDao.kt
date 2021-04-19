package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ustadmobile.core.db.dao.ContentEntryProgressDao.Companion.FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryProgress

@Repository
@Dao
abstract class ContentEntryProgressDao : BaseDao<ContentEntryProgress> {

    @Update
    abstract suspend fun updateAsync(contentEntryProgress: ContentEntryProgress): Int

    @Query(FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY)
    abstract fun getProgressByContentAndPerson(contentEntryUid: Long, personUid: Long): ContentEntryProgress?

    @Query(FIND_PROGRESS_BY_CONTENT_AND_PERSON_QUERY)
    abstract suspend fun getProgressByContentAndPersonAsync(contentEntryUid: Long, personUid: Long): ContentEntryProgress?

    @Query("""UPDATE ContentEntryProgress SET contentEntryProgressProgress = :progress, 
                    contentEntryProgressStatusFlag = :status,
                    contentEntryProgressLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0)
                    WHERE 
                    contentEntryProgressPersonUid = :personUid AND 
                    contentEntryProgressContentEntryUid = :contentEntryUid
                    """)
    abstract fun updateProgressByContentEntryAndPerson(contentEntryUid: Long, personUid: Long,
                                                       progress: Int, status: Int): Int

    @Transaction
    open fun updateProgress(entryUuid: Long, personUid: Long, currentProgress: Int, statusFlag: Int) {
        val progressOnDb = getProgressByContentAndPerson(entryUuid, personUid)
                ?: ContentEntryProgress().apply {
                    contentEntryProgressActive = true
                    contentEntryProgressProgress = currentProgress
                    contentEntryProgressContentEntryUid = entryUuid
                    contentEntryProgressPersonUid = personUid
                    contentEntryProgressStatusFlag = statusFlag
                    contentEntryProgressUid = insert(this)
                }

        if (currentProgress > progressOnDb.contentEntryProgressProgress) {
            updateProgressByContentEntryAndPerson(entryUuid, personUid, currentProgress, statusFlag)
        }
    }

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
