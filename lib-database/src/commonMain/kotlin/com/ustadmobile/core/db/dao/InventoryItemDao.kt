package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.InventoryTransaction
import com.ustadmobile.lib.db.entities.PersonWithInventory
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
    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
    SaleProduct.saleProductUid = :saleProductUid
    """)
    abstract suspend fun findStockForSaleProduct(saleProductUid: Long): Int


    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_PERSON_ASC)
    abstract suspend fun findStockByPersonAsc(saleProductUid: Long) : List<PersonWithInventory>

    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_PERSON_DESC)
    abstract suspend fun findStockByPersonDesc(saleProductUid: Long) : List<PersonWithInventory>

    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_STOCK_ASC)
    abstract suspend fun findStockByPersonStockAsc(saleProductUid: Long) : List<PersonWithInventory>

    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_STOCK_DESC)
    abstract suspend fun findStockByPersonStockDesc(saleProductUid: Long) : List<PersonWithInventory>

    @Insert
    abstract suspend fun insertTransaction(inventoryTransaction: InventoryTransaction)

    @Query(QUERY_FIND_AVAILABLE_INVENTORY_ITEMS_FOR_PRODUCT)
    abstract suspend fun findAvailableInventoryItemsByProduct(saleProductUid: Long): List<InventoryItem>


    @Query(QUERY_FIND_AVAILABLE_INVENTORY_ITEMS_FOR_PRODUCT + " LIMIT :count ")
    abstract suspend fun findAvailableInventoryItemsByProductLimit(saleProductUid: Long,
                                                                   count:Int): List<InventoryItem>

    suspend fun insertInventoryItem(item: InventoryItem, count: Int, leUid:Long){

        var x =0
        while(x < count){

            val inventoryItemUid = insert(item)
            val inventoryTransaction = InventoryTransaction(inventoryItemUid, leUid)
            insertTransaction(inventoryTransaction)

            x++;
        }
    }

    @Query("""
        SELECT SaleProduct.*, COUNT(*) as stock FROM InventoryItem 
        LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
        WHERE CAST(SaleProduct.saleProductActive AS INTEGER) = 1 
        AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
        GROUP BY(SaleProduct.saleProductUid)
        ORDER BY InventoryItem.inventoryItemDateAdded DESC 
    """)
    abstract fun findAllInventoryByProduct(): DataSource.Factory<Int, SaleProductWithInventoryCount>


    companion object{
        const val QUERY_BY_PERSON = """
        SELECT WE.*, COUNT(*) as inventoryCount
        FROM InventoryItem
        LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
        LEFT JOIN Person AS WE ON WE.personUid = InventoryItem.InventoryItemWeUid
        WHERE
        CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND
        CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
        SaleProduct.saleProductUid = :saleProductUid
        GROUP BY We.personUid 
    """
        const val QUERY_SORT_BY_PERSON_ASC = " ORDER BY WE.firstNames||' '||WE.lastName ASC "
        const val QUERY_SORT_BY_PERSON_DESC = " ORDER BY WE.firstNames||' '||WE.lastName DESC "
        const val QUERY_SORT_BY_STOCK_ASC = " ORDER BY inventoryCount ASC "
        const val QUERY_SORT_BY_STOCK_DESC = " ORDER BY inventoryCount DESC "

        const val QUERY_FIND_AVAILABLE_INVENTORY_ITEMS_FOR_PRODUCT =
                """
                   SELECT * FROM InventoryItem  WHERE 
                    InventoryItem.InventoryItemUid NOT IN (SELECT InventoryItem.inventoryItemUid FROM InventoryTransaction 
                    LEFT JOIN InventoryItem ON InventoryItem.InventoryItemUid = InventoryTransaction.InventoryTransactionInventoryItemUid
                    WHERE 
                    InventoryItem.InventoryItemSaleProductUid = :saleProductUid AND InventoryTransaction.InventoryTransactionSaleUid != 0)
                    AND InventoryItem.InventoryItemSaleProductUid = :saleProductUid 
                """


    }

}
