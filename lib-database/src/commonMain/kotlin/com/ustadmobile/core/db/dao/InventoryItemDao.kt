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
    SELECT COUNT(*) - 
                (select count(*) from inventorytransaction 
                where 
                inventorytransaction.inventoryTransactionInventoryItemUid in 
                (select inventoryitemuid from inventoryitem where 
                inventoryitem.inventoryitemsaleproductuid = SaleProduct.saleProductUid
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 ) 
                and inventorytransaction.inventoryTransactionSaleUid != 0
                and cast(inventorytransaction.inventorytransactionactive as integer) = 1 ) 
    FROM InventoryItem
    LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
    LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
    WHERE
    CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND
    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
    SaleProduct.saleProductUid = :saleProductUid 
    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 
    AND (InventoryItem.InventoryItemWeUid IN (
        SELECT MEMBER.personUid FROM PersonGroupMember 
        LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
        LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
         WHERE groupMemberGroupUid = LE.mPersonGroupUid 
        AND CAST(groupMemberActive  AS INTEGER) = 1
        ) OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 1 END )
        AND (InventoryItem.inventoryItemLeUid = :leUid 
		OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END )
    """)
    abstract suspend fun findStockForSaleProduct(saleProductUid: Long,
                                                 leUid: Long): Int


    @Query("""
    SELECT COUNT(*) - 
                (select count(*) from inventorytransaction 
                where 
                inventorytransaction.inventoryTransactionInventoryItemUid in 
                (select inventoryitemuid from inventoryitem where 
                inventoryitem.inventoryitemsaleproductuid = SaleProduct.saleProductUid
                AND InventoryItem.inventoryItemWeUid = :weUid ) 
                and inventorytransaction.inventoryTransactionSaleUid != 0) AS inventoryCount
    FROM InventoryItem
    LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
    LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
    LEFT JOIN PERSON AS WE ON WE.personUid = :weUid
    WHERE
    CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND
    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
    SaleProduct.saleProductUid = :saleProductUid 
    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 
    AND (InventoryItem.InventoryItemWeUid IN (
        SELECT MEMBER.personUid FROM PersonGroupMember 
        LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
        LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
         WHERE groupMemberGroupUid = LE.mPersonGroupUid 
        AND CAST(groupMemberActive  AS INTEGER) = 1
        ) OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 1 END )
        AND (InventoryItem.inventoryItemLeUid = :leUid 
		OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END )
    AND InventoryItem.inventoryItemWeUid = :weUid
    """)
    abstract suspend fun findStockForSaleProductByWeUid(saleProductUid: Long,
                                                 leUid: Long, weUid: Long): Int


    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_PERSON_ASC)
    abstract suspend fun findStockByPersonAsc(saleProductUid: Long, leUid: Long) : List<PersonWithInventory>

    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_PERSON_DESC)
    abstract suspend fun findStockByPersonDesc(saleProductUid: Long, leUid: Long) : List<PersonWithInventory>

    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_STOCK_ASC)
    abstract suspend fun findStockByPersonStockAsc(saleProductUid: Long, leUid: Long) : List<PersonWithInventory>

    @Query(QUERY_BY_PERSON + QUERY_SORT_BY_STOCK_DESC)
    abstract suspend fun findStockByPersonStockDesc(saleProductUid: Long, leUid: Long) : List<PersonWithInventory>

    @Insert
    abstract suspend fun insertTransaction(inventoryTransaction: InventoryTransaction)

    @Query(QUERY_FIND_AVAILABLE_INVENTORY_ITEMS_FOR_PRODUCT)
    abstract suspend fun findAvailableInventoryItemsByProduct(saleProductUid: Long, leUid: Long, weUid: Long): List<InventoryItem>


    @Query(QUERY_FIND_AVAILABLE_INVENTORY_ITEMS_FOR_PRODUCT + " LIMIT :count ")
    abstract suspend fun findAvailableInventoryItemsByProductLimit(saleProductUid: Long,
                                                                   count:Int, leUid: Long, weUid : Long): List<InventoryItem>

    suspend fun insertInventoryItem(item: InventoryItem, count: Int, leUid:Long){

        var x =0
        while(x < count){

            val inventoryItemUid = insert(item)
            val inventoryTransaction = InventoryTransaction(inventoryItemUid, leUid)
            insertTransaction(inventoryTransaction)

            x++;
        }
    }

    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_NAME_ASC)
    abstract fun findAllInventoryByProductNameAsc(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>

    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_NAME_DESC)
    abstract fun findAllInventoryByProductNameDesc(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>
    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_STOCK_ASC)
    abstract fun findAllInventoryByProductStockAsc(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>
    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_STOCK_DESC)
    abstract fun findAllInventoryByProductStockDesc(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>
    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_MOST_RECENT)
    abstract fun findAllInventoryByProductMostRecent(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>
    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_LEAST_RECENT)
    abstract fun findAllInventoryByProductLeastRecent(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>


    fun findAllInventoryByProduct(leUid: Long, searchBit: String, sortCode: Int)
            : DataSource.Factory<Int, SaleProductWithInventoryCount>{

        when(sortCode){
            SORT_ORDER_NAME_ASC -> {return findAllInventoryByProductNameAsc(leUid, searchBit)}
            SORT_ORDER_NAME_DESC -> {return findAllInventoryByProductNameDesc(leUid, searchBit)}
            SORT_ORDER_STOCK_ASC -> {return findAllInventoryByProductStockAsc(leUid, searchBit)}
            SORT_ORDER_STOCK_DESC -> {return findAllInventoryByProductStockDesc(leUid, searchBit)}
            SORT_ORDER_MOST_RECENT -> {return findAllInventoryByProductMostRecent(leUid, searchBit)}
            SORT_ORDER_LEAST_RECENT -> {return findAllInventoryByProductLeastRecent(leUid, searchBit)}
            else -> {return findAllInventoryByProductMostRecent(leUid, searchBit)}
        }

    }

    companion object{

        const val QUERY_BY_PERSON = """
        SELECT 
            Person.*,
            (
                SELECT 
                    ( COUNT(*) - 
                        (	select count(*) from inventorytransaction 
                            where 
                            inventorytransaction.inventoryTransactionInventoryItemUid in 
                            (	select inventoryitemuid from inventoryitem where 
                                inventoryitem.inventoryitemsaleproductuid = SaleProduct.saleProductUid
                                AND InventoryItem.inventoryItemWeUid = Person.personUid
                                AND InventoryItem.inventoryItemLeUid = MLE.personUid
                                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                            ) 
                            and inventorytransaction.inventoryTransactionSaleUid != 0 
                            and cast(inventorytransaction.inventoryTransactionActive AS INTEGER) = 1 )
                    ) 
                FROM InventoryItem
                    LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = InventoryItemSaleProductUid
                WHERE
                    CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND
                    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
                    SaleProduct.saleProductUid = :saleProductUid 
                    AND InventoryItem.inventoryItemWeUid = Person.personUid
                    AND InventoryItem.inventoryItemLeUid = MLE.personUid
                    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
            ) as inventoryCount
        FROM Person
        LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
        WHERE 
            CAST(Person.mPersonGroupUid AS INTEGER)= 0 AND 
            CAST(Person.admin AS INTEGER) = 0 AND 
            CAST(Person.personRoleUid AS INTEGER) = 0 AND 
            CAST(Person.active AS INTEGER) = 1 AND
            (Person.personUid IN (
                SELECT MEMBER.personUid FROM PersonGroupMember 
                LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
                 WHERE groupMemberGroupUid = MLE.mPersonGroupUid 
                AND CAST(groupMemberActive  AS INTEGER) = 1 ) 
                OR 
                CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END )
            
        """
        const val QUERY_SORT_BY_PERSON_ASC = " ORDER BY Person.firstNames||' '||Person.lastName ASC "
        const val QUERY_SORT_BY_PERSON_DESC = " ORDER BY Person.firstNames||' '||Person.lastName DESC "
        const val QUERY_SORT_BY_STOCK_ASC = " ORDER BY inventoryCount ASC "
        const val QUERY_SORT_BY_STOCK_DESC = " ORDER BY inventoryCount DESC "

        const val QUERY_FIND_AVAILABLE_INVENTORY_ITEMS_FOR_PRODUCT =
                """
                   SELECT * 
                    FROM InventoryItem item  WHERE 
                    
                    item.InventoryItemUid NOT IN 
                        (SELECT AII.inventoryItemUid FROM InventoryTransaction AS transactions
                        LEFT JOIN InventoryItem AS AII ON AII.InventoryItemUid = transactions.InventoryTransactionInventoryItemUid
                        WHERE 
                        AII.InventoryItemSaleProductUid = :saleProductUid AND transactions.InventoryTransactionSaleUid != 0
                        AND CAST(AII.inventoryItemActive AS INTEGER) = 1 AND CAST(transactions.inventorytransactionactive AS INTEGER) = 1)
                        
                    AND item.InventoryItemSaleProductUid = :saleProductUid
                    AND CAST(item.inventoryItemActive AS INTEGER) = 1
                    AND item.inventoryItemLeUid = :leUid
                    AND item.inventoryItemWeUid = :weUid
                    AND CAST(item.inventoryItemActive AS INTEGER) = 1
                """

        const val QUERY_INVENTORY_LIST = """
            SELECT SaleProduct.*, 
            COUNT(*) - 
                (select count(*) from inventorytransaction 
                where 
                inventorytransaction.inventoryTransactionInventoryItemUid in 
                (select inventoryitemuid from inventoryitem where 
                inventoryitem.inventoryitemsaleproductuid = SaleProduct.saleProductUid
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 ) 
                and inventorytransaction.inventoryTransactionSaleUid != 0 
                and cast(inventorytransaction.inventorytransactionactive as integer) = 1 )   
            as stock 
            FROM InventoryItem 
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
            ) OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 1 END )
            AND (InventoryItem.inventoryItemLeUid = :leUid 
            OR CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END )
    
            GROUP BY(SaleProduct.saleProductUid)
        """

        const val QUERY_INVENTORY_LIST_SORTBY_NAME_ASC =
                " ORDER BY SaleProduct.saleProductName ASC "
        const val QUERY_INVENTORY_LIST_SORTBY_NAME_DESC =
                " ORDER BY SaleProduct.saleProductName DESC "
        const val QUERY_INVENTORY_LIST_SORTBY_STOCK_ASC =
                " ORDER BY stock ASC "
        const val QUERY_INVENTORY_LIST_SORTBY_STOCK_DESC =
                " ORDER BY stock DESC "
        const val QUERY_INVENTORY_LIST_SORTBY_MOST_RECENT =
                " ORDER BY InventoryItem.inventoryItemDateAdded DESC "
        const val QUERY_INVENTORY_LIST_SORTBY_LEAST_RECENT =
                " ORDER BY InventoryItem.inventoryItemDateAdded ASC "

        const val SORT_ORDER_NAME_ASC = 1
        const val SORT_ORDER_NAME_DESC = 2
        const val SORT_ORDER_STOCK_ASC = 3
        const val SORT_ORDER_STOCK_DESC = 4
        const val SORT_ORDER_MOST_RECENT = 5
        const val SORT_ORDER_LEAST_RECENT = 6
    }

}
