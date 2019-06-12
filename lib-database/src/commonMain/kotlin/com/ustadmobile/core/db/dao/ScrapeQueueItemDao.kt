package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.lib.db.entities.ScrapeQueueItem

@Dao
abstract class ScrapeQueueItemDao : BaseDao<ScrapeQueueItem> {

    @Transaction
    open fun getNextItemAndSetStatus(runId: Int, itemType: Int): ScrapeQueueItem? {
        val nextItem = findNextItem(STATUS_PENDING, runId, itemType)
        if (nextItem != null) {
            updateSetStatusById(nextItem.sqiUid, STATUS_RUNNING)
        }

        return nextItem
    }

    @Query("SELECT * FROM ScrapeQueueItem")
    abstract fun findAll(): List<ScrapeQueueItem>


    @Query("SELECT * FROM ScrapeQueueItem WHERE status = :status AND runId = :runId AND itemType = :itemType LIMIT 1")
    abstract fun findNextItem(status: Int, runId: Int, itemType: Int): ScrapeQueueItem?

    @Query("UPDATE ScrapeQueueItem SET status = :status WHERE sqiUid = :uid")
    abstract fun updateSetStatusById(uid: Int, status: Int)

    @Query("SELECT * from ScrapeQueueItem WHERE runId = :runId AND scrapeUrl = :indexUrl LIMIT 1")
    abstract fun getExistingQueueItem(runId: Int, indexUrl: String): ScrapeQueueItem?

    @Query("UPDATE ScrapeQueueItem SET timeStarted = :timeStarted WHERE sqiUid = :uid")
    abstract fun setTimeStarted(uid: Int, timeStarted: Long)

    @Query("UPDATE ScrapeQueueItem SET timeFinished = :timeFinished WHERE sqiUid = :uid")
    abstract fun setTimeFinished(uid: Int, timeFinished: Long)

    companion object {

        const val STATUS_PENDING = 1

        const val STATUS_RUNNING = 2

        const val STATUS_DONE = 3

        const val STATUS_FAILED = 4
    }

}
