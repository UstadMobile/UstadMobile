package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.DashboardEntry

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@Dao
@UmRepository
abstract class DashboardEntryDao : BaseDao<DashboardEntry> {


    @Query("UPDATE DashboardEntry SET dashboardEntryTitle = :title " +
            " WHERE dashboardEntryUid = :uid")
    abstract suspend fun updateTitle(uid: Long, title: String):Int

    @Query("SELECT * FROM DashboardEntry WHERE " + "dashboardEntryPersonUid = :uid " +
            " AND CAST(dashboardEntryActive AS INTEGER) = 1 ORDER BY dashboardEntryIndex ASC")
    abstract fun findByPersonAndActiveProvider(uid: Long): DataSource.Factory<Int,DashboardEntry>


    @Query("UPDATE DashboardEntry SET dashboardEntryIndex = -1 " +
            " WHERE  dashboardEntryUid = :uid")
    abstract suspend fun pinEntry(uid: Long):Int

    @Query("UPDATE DashboardEntry SET dashboardEntryIndex = " +
            "(SELECT (SELECT MAX(dashboardEntryIndex) FROM DashboardEntry) +1) " +
            "WHERE dashboardEntryUid = :uid")
    abstract suspend fun unpinEntry(uid: Long):Int

    //TODO: Make it use a boolean argument
    @Query("UPDATE DashboardEntry SET dashboardEntryActive = 0 " +
            " WHERE dashboardEntryUid = :uid")
    abstract suspend fun deleteEntry(uid: Long):Int

    @Query("SELECT * FROM DashboardEntry WHERE dashboardEntryUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long):DashboardEntry?

    @Update
    abstract suspend fun updateAsync(entity: DashboardEntry):Int

}
