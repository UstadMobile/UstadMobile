package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.DashboardEntryTag


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class DashboardEntryTagDao : SyncableDao<DashboardEntryTag, DashboardEntryTagDao> {

    @Query("SELECT * FROM DashboardEntryTag WHERE dashboardEntryTagActive = 1")
    abstract fun findAllActiveProvider(): DataSource.Factory<Int, DashboardEntryTag>

    @Query("SELECT * FROM DashboardEntryTag WHERE dashboardEntryTagActive = 1 " +
            " AND dashboardEntryTagDashboardEntryUid = :uid ")
    abstract fun findByEntryProvider(uid: Long): DataSource.Factory<Int, DashboardEntryTag>

    @Query("SELECT * FROM DashboardEntryTag WHERE dashboardEntryTagActive = 1 " +
            " AND dashboardEntryTagDashboardTagUid = :uid ")
    abstract fun findByTagProvider(uid: Long): DataSource.Factory<Int, DashboardEntryTag>


}
