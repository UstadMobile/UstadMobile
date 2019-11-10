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
abstract class DashboardEntryTagDao
    : BaseDao<DashboardEntryTag> {

    @Query("SELECT * FROM DashboardEntryTag WHERE CAST(dashboardEntryTagActive AS INTEGER) = 1 ")
    abstract fun findAllActiveProvider(): DataSource.Factory<Int, DashboardEntryTag>

    @Query("SELECT * FROM DashboardEntryTag WHERE CAST(dashboardEntryTagActive AS INTEGER) = 1  "
            + " AND dashboardEntryTagDashboardEntryUid = :uid ")
    abstract fun findByEntryProvider(uid: Long)
            : DataSource.Factory<Int, DashboardEntryTag>

    @Query("SELECT * FROM DashboardEntryTag WHERE CAST(dashboardEntryTagActive AS INTEGER) = 1  "
            + " AND dashboardEntryTagDashboardTagUid = :uid ")
    abstract fun findByTagProvider(uid: Long)
            : DataSource.Factory<Int, DashboardEntryTag>


}
