package com.ustadmobile.core.db.dao


import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleItemReminder

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleItemReminderDao : BaseDao<SaleItemReminder> {


    @Query("SELECT * FROM SaleItemReminder WHERE SaleItemReminderSaleItemUid = :uid AND " +
            " saleItemReminderActive =1 AND CAST(saleItemReminderActive AS INTEGER) = 1")
    abstract fun findBySaleItemUid(uid: Long): DataSource.Factory<Int, SaleItemReminder>

    @Query("SELECT * FROM SaleItemReminder WHERE SaleItemReminderSaleItemUid = :uid " +
            "AND CAST(saleItemReminderActive AS INTEGER) = 1")
    abstract fun findBySaleItemUidList(uid: Long): List<SaleItemReminder>

    //TODO: Replace with Boolean argument
    @Query("UPDATE SaleItemReminder SET SaleItemReminderActive = 0 " +
            "WHERE SaleItemReminderUid = :uid " + "AND saleItemReminderActive")
    abstract suspend fun invalidateReminder(uid: Long):Int

    @Query("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND CAST(saleItemReminderActive AS INTEGER) = 1")
    abstract suspend fun findBySaleItemUidAsync(uid: Long):List<SaleItemReminder>

    @Query("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND CAST(saleItemReminderActive AS INTEGER) = 1")
    abstract fun findBySaleItemUidLive(uid: Long):DoorLiveData<List<SaleItemReminder>>

    @Query("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderDays = :days AND CAST(saleItemReminderActive AS INTEGER) = 1")
    abstract suspend fun findBySaleItemUidAndDaysAsync(uid: Long, days: Int): List<SaleItemReminder>

}
