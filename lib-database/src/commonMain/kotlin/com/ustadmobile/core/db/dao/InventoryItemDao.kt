package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.SaleProductWithInventoryCount


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class InventoryItemDao: BaseDao<InventoryItem> {

    @Query("""
    SELECT COUNT(*)
    FROM InventoryItem
    LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
    WHERE
    CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND
    CAST(InventoryItem.InventoryItemActive AS INTEGER) = 1 AND
    SaleProduct.saleProductUid = :saleProductUid
    """)
    abstract suspend fun fingStockForSaleProduct(saleProductUid: Long): Int



    @Query("""
        SELECT SaleProduct.*, COUNT(*) FROM InventoryItem as stock
        LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
        WHERE CAST(SaleProduct.saleProductActive AS INTEGER) = 1 
        AND CAST(InventoryItem.InventoryItemActive AS INTEGER) = 1
        GROUP BY(SaleProduct.saleProductUid)
        ORDER BY InventoryItem.inventoryItemDateAdded DESC 
    """)
    abstract fun findAllInventoryByProduct(): DataSource.Factory<Int, SaleProductWithInventoryCount>


}
