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

    //TODO: Only show my WE's contibution to this product
    @Query("""
        SELECT SaleProduct.*, COUNT(*) as stock FROM InventoryItem 
        LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
        LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
        WHERE CAST(SaleProduct.saleProductActive AS INTEGER) = 1 
        AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
        AND saleProductName LIKE :searchBit
        
        AND (InventoryItem.InventoryItemWeUid IN (
        SELECT MEMBER.personUid FROM PersonGroupMember 
        LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
        LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
         WHERE groupMemberGroupUid = LE.mPersonGroupUid 
        AND CAST(groupMemberActive  AS INTEGER) = 1
        ) OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 0 ELSE 1 END )
        
        GROUP BY(SaleProduct.saleProductUid)
        ORDER BY InventoryItem.inventoryItemDateAdded DESC 
    """)
    abstract fun findAllInventoryByProduct(leUid: Long, searchBit: String): DataSource.Factory<Int,
            SaleProductWithInventoryCount>


    companion object{
        const val QUERY_BY_PERSON_ONLY = """
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

        const val QUERY_BY_PERSON = """
           SELECT 
                Person.*,
                (SELECT COUNT(*)
                FROM InventoryItem
                LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
                LEFT JOIN Person AS WE ON WE.personUid = InventoryItem.InventoryItemWeUid
                WHERE
                    CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND
                    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
                    SaleProduct.saleProductUid = :saleProductUid AND
                    WE.personUid = Person.personUid
                ) as inventoryCount
            FROM Person
            WHERE 
                CAST(Person.mPersonGroupUid AS INTEGER)= 0 AND 
                CAST(Person.admin AS INTEGER) = 0 AND 
                CAST(PersonRoleUid AS INTEGER) = 0 AND 
                CAST(Person.active AS INTEGER) = 1
            
        """
        const val QUERY_SORT_BY_PERSON_ASC = " ORDER BY Person.firstNames||' '||Person.lastName ASC "
        const val QUERY_SORT_BY_PERSON_DESC = " ORDER BY Person.firstNames||' '||Person.lastName DESC "
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

        const val SORT_ORDER_NAME_ASC = 1
        const val SORT_ORDER_NAME_DESC = 2
        const val SORT_ORDER_STOCK_ASC = 3
        const val SORT_ORDER_STOCK_DESC = 4
        const val SORT_ORDER_MOST_RECENT = 5
        const val SORT_ORDER_LEAST_RECENT = 6
    }

}
