package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ScrapeQueueItem

@Dao
abstract class ScrapeQueueItemDao : BaseDao<ScrapeQueueItem> {


    @Query("SELECT * FROM ScrapeQueueItem")
    abstract fun findAll(): List<ScrapeQueueItem>

    @Query("SELECT * FROM ScrapeQueueItem WHERE status = $STATUS_PENDING AND itemType = :itemType ORDER BY priority ASC LIMIT 10")
    abstract fun findNextQueueItems(itemType: Int): DoorLiveData<List<ScrapeQueueItem>>

    @Query("UPDATE ScrapeQueueItem SET status = :status, errorCode = :errorCode WHERE sqiUid = :uid")
    abstract fun updateSetStatusById(uid: Int, status: Int, errorCode: Int)

    @Query("SELECT * from ScrapeQueueItem WHERE runId = :runId AND scrapeUrl = :indexUrl LIMIT 1")
    abstract fun getExistingQueueItem(runId: Int, indexUrl: String): ScrapeQueueItem?

    @Query("SELECT * from ScrapeQueueItem WHERE runId = :runId AND sqiContentEntryUid = :entryUid LIMIT 1")
    abstract fun findExistingQueueItem(runId: Int, entryUid: Long): ScrapeQueueItem?

    @Query("UPDATE ScrapeQueueItem SET timeStarted = :timeStarted WHERE sqiUid = :uid")
    abstract fun setTimeStarted(uid: Int, timeStarted: Long)

    @Query("UPDATE ScrapeQueueItem SET timeFinished = :timeFinished WHERE sqiUid = :uid")
    abstract fun setTimeFinished(uid: Int, timeFinished: Long)

    @Query("SELECT COUNT(*) FROM ScrapeQueueItem WHERE (status = $STATUS_PENDING OR status = $STATUS_RUNNING) AND runId = :runUid")
    abstract fun getQueueCount(runUid: Int): Int

    companion object {

        const val STATUS_PENDING = 1

        const val STATUS_RUNNING = 2

        const val STATUS_DONE = 3

        const val STATUS_FAILED = 4

    }

}
