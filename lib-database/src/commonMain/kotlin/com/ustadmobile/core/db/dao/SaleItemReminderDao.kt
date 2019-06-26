package com.ustadmobile.core.db.dao


import androidx.room.Query
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleItemReminder

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleItemReminderDao : SyncableDao<SaleItemReminder, SaleItemReminderDao> {

    @Query("SELECT * FROM SaleItemReminder WHERE SaleItemReminderSaleItemUid = :uid AND " +
            " saleItemReminderActive =1 AND saleItemReminderActive = 1")
    abstract fun findBySaleItemUid(uid: Long): UmProvider<SaleItemReminder>

    @Query("SELECT * FROM SaleItemReminder WHERE SaleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderActive = 1")
    abstract fun findBySaleItemUidList(uid: Long): List<SaleItemReminder>

    @Query("UPDATE SaleItemReminder SET SaleItemReminderActive = 0 " +
            "WHERE SaleItemReminderUid = :uid " + "AND saleItemReminderActive = 1")
    abstract fun invalidateReminder(uid: Long, resultCallback: UmCallback<Int>)

    @Query("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderActive = 1")
    abstract suspend fun findBySaleItemUidAsync(uid: Long):List<SaleItemReminder>

    @Query("SELECT * FROM SaleItemReminder WHERE saleItemReminderSaleItemUid = :uid " +
            "AND saleItemReminderDays = :days AND saleItemReminderActive = 1")
    abstract suspend fun findBySaleItemUidAndDaysAsync(uid: Long, days: Int): List<SaleItemReminder>
}
