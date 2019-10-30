package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory

/**
 * Created by mike on 2/2/18.
 */

@Dao
abstract class DownloadJobItemHistoryDao {

    @Query("SELECT * FROM DownloadJobItemHistory WHERE networkNode = :nodeId AND startTime >= :since")
    abstract fun findHistoryItemsByNetworkNodeSince(nodeId: Long, since: Long): List<DownloadJobItemHistory>

    @Insert
    abstract fun insert(downloadJobItemHistory: DownloadJobItemHistory): Long

    @Update
    abstract fun update(downloadJobItemHistory: DownloadJobItemHistory)

    @Query("DELETE FROM DownloadJobItemHistory")
    abstract suspend fun deleteAllAsync()

    @Insert
    abstract fun insertList(historyList: List<DownloadJobItemHistory>)


    @Query("SELECT * From DownloadJobItemHistory WHERE downloadJobItemId = :downloadJobItemId")
    abstract fun findHistoryItemsByDownloadJobItem(downloadJobItemId: Int): List<DownloadJobItemHistory>
}
