package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryTransaction
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class InventoryTransactionDao: BaseDao<InventoryTransaction> {


    @Query("""
    SELECT 
        COUNT(*) as stockCount,  
        GROUP_CONCAT(DISTINCT WE.firstNames||' '||WE.lastName) as weNames,  
        Sale.saleUid, TOLE.personUid as toLeUid,
        CASE WHEN Sale.saleUid THEN Sale.saleCreationDate ELSE InventoryItem.InventoryItemDateAdded END as transactionDate
    FROM InventoryTransaction
    LEFT JOIN InventoryItem ON InventoryTransaction.inventoryTransactionInventoryItemUid = InventoryItem.inventoryItemUid
    LEFT JOIN SaleProduct ON InventoryItem.InventoryItemSaleProductUid = SaleProduct.saleProductUid 
    LEFT JOIN Person as TOLE ON InventoryTransaction.InventoryTransactionToLeUid = TOLE.personUid 
    LEFT JOIN Person as LE ON InventoryTransaction.inventoryTransactionFromLeUid = LE.personUid
    LEFT JOIN Person as WE ON InventoryItem.InventoryItemWeUid = WE.personUid
    LEFT JOIN Sale ON InventoryTransaction.inventoryTransactionSaleUid = Sale.saleUid
    WHERE 
        CAST(InventoryTransaction.inventoryTransactionActive AS INTEGER) = 1 AND 
        SaleProduct.saleProductUid = :saleProductUid AND 
        (LE.personUid = :leUid OR CASE WHEN (CAST(LE.admin as INTEGER) = 1) THEN 0 ELSE 1 END )
    GROUP BY saleUid, transactionDate
    """)
    abstract fun findAllInventoryByProduct(saleProductUid: Long, leUid: Long)
            : DataSource.Factory<Int, InventoryTransactionDetail>


}
