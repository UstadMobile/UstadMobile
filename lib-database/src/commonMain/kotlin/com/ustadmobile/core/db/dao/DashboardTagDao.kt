package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.DashboardTag


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class DashboardTagDao : BaseDao<DashboardTag> {

    @Query("SELECT * FROM DashboardTag WHERE CAST(dashboardTagActive AS INTEGER) = 1 ")
    abstract fun findAllActiveProvider(): DataSource.Factory<Int, DashboardTag>


    @Query("SELECT * FROM DashboardTag WHERE CAST(dashboardTagActive AS INTEGER) = 1 ")
    abstract fun findAllActiveLive(): DoorLiveData<List<DashboardTag>>


}
