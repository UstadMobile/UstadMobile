package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin

@Dao
abstract class DownloadJobItemParentChildJoinDao {

    @Query("SELECT DownloadJobItemParentChildJoin.* FROM DownloadJobItemParentChildJoin " + "WHERE djiChildDjiUid = :childDjiUid ")
    abstract fun findParentsByChildUid(childDjiUid: Int): List<DownloadJobItemParentChildJoin>

    @Query("SELECT DownloadJobItemParentChildJoin.* FROM DownloadJobItemParentChildJoin " +
            " LEFT JOIN DownloadJobItem ON DownloadJobItemParentChildJoin.djiParentDjiUid = DownloadJobItem.djiUid " +
            " WHERE DownloadJobItem.djiDjUid = :djUid")
    abstract fun findParentChildJoinsByDownloadJobUids(djUid: Int): List<DownloadJobItemParentChildJoin>

    @Insert
    abstract fun insertList(joins: List<DownloadJobItemParentChildJoin>)

    @Insert
    abstract fun insert(jobRunItem: DownloadJobItemParentChildJoin): Long

}
