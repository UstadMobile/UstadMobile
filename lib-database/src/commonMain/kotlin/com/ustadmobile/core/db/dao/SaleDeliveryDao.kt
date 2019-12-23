package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository

import com.ustadmobile.lib.db.entities.SaleDelivery


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleDeliveryDao: BaseDao<SaleDelivery> {


    @Query("""
            SELECT * FROM SaleDelivery WHERE saleDeliveryUid = :uid 
            AND CAST(SaleDelivery.saleDeliveryActive AS INTEGER) = 1 
        """)
    abstract suspend fun findByUidAsync(uid: Long): SaleDelivery?

    @Update
    abstract suspend fun updateAsync(entity: SaleDelivery)


    @Query("""
        SELECT SaleDelivery.* FROM SaleDelivery 
        WHERE SaleDelivery.saleDeliverySaleUid = :saleUid 
            AND CAST(SaleDelivery.saleDeliveryActive AS INTEGER) = 1
    """)
    abstract fun findAllDeliveriesBySaleUid(saleUid: Long): DataSource.Factory<Int, SaleDelivery>


}
