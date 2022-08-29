package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.core.db.dao.ScrapeQueueItemDaoCommon.STATUS_PENDING
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.db.entities.ScrapeQueueItemWithScrapeRun

@DoorDao
expect abstract class ScrapeQueueItemDao : BaseDao<ScrapeQueueItem> {


    @Query("SELECT * FROM ScrapeQueueItem WHERE status = $STATUS_PENDING AND itemType = :itemType ORDER BY priority ASC LIMIT 10")
    abstract fun findNextQueueItems(itemType: Int): LiveData<List<ScrapeQueueItem>>

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

    @Query("""SELECT ScrapeQueueItem.*, ScrapeRun.* FROM ScrapeQueueItem 
                    LEFT JOIN ScrapeRun ON  ScrapeQueueItem.runId = ScrapeRun.scrapeRunUid
                    WHERE ScrapeQueueItem.sqiUid = :sqiUid""")
    abstract fun findByUid(sqiUid: Int): ScrapeQueueItemWithScrapeRun?


}
