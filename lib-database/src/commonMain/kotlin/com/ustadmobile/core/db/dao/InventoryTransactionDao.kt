package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryTransaction
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import com.ustadmobile.lib.db.entities.PersonWithInventoryCount
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class InventoryTransactionDao : BaseDao<InventoryTransaction> {

    @Update
    abstract suspend fun updateAsync(entity: InventoryTransaction): Int

    @Query(QUERY_GET_STOCK_LIST_BY_PRODUCT)
    abstract fun getStockListByProduct(productUid: Long, leUid: Long) :
            DataSource.Factory<Int, PersonWithInventoryCount>

    @Query(QUERY_GET_PRODUCT_TRANSACTION_HISTORY)
    abstract fun getProductTransactionDetail(productUid: Long, leUid: Long) :
            DataSource.Factory<Int, InventoryTransactionDetail>

    companion object{


        const val QUERY_INVENTORY_LIST_SORTBY_NAME_ASC =
                " ORDER BY Product.productName ASC "


        const val QUERY_GET_STOCK_LIST_BY_PRODUCT = """
            SELECT WE.*, 
            (
                SELECT 
                    ( COUNT(*) - 
                        (	select count(*) from inventorytransaction 
                            where 
                            inventorytransaction.inventoryTransactionInventoryItemUid in 
                            (	select inventoryitemuid from inventoryitem where 
                                inventoryitem.inventoryitemproductuid = Product.productUid
                                AND InventoryItem.inventoryItemWeUid = WE.personUid
                                AND InventoryItem.inventoryItemLeUid = :leUid
                                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                            ) 
                            and inventorytransaction.inventoryTransactionSaleUid != 0 
                            and cast(inventorytransaction.inventoryTransactionActive AS INTEGER) = 1 )
                    ) 
                FROM inventorytransaction
					LEFT JOIN InventoryItem on InventoryItem.inventoryItemUid = InventoryTransaction.inventoryTransactionInventoryItemUid
                    LEFT JOIN Product ON Product.productUid = inventoryItemProductUid
                WHERE
                    CAST(Product.productActive AS INTEGER) = 1 AND
                    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
                    Product.productUid = :productUid 
                    AND InventoryItem.inventoryItemWeUid = WE.personUid
                    AND InventoryItem.inventoryItemLeUid = :leUid
                    and inventorytransaction.inventoryTransactionSaleUid == 0
                    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
            )  as inventoryCount, 
            0 as inventoryCountDeliveredTotal, 
            0 as inventoryCountDelivered, 
            0 as inventorySelected
            FROM PersonGroupMember
            LEFT JOIN Person AS WE ON WE.personUid = PersonGroupMember.groupMemberPersonUid
             LEFT JOIN Person AS LE ON LE.personUid = :leUid
            LEFT JOIN Product ON Product.productUid = :productUid
            
            WHERE 
            CAST(LE.active AS INTEGER) = 1 
            AND PersonGroupMember.groupMemberGroupUid = LE.personWeGroupUid 
            AND CAST(WE.active AS INTEGER) = 1  
            GROUP BY(WE.personUid)  
            
        """

        const val QUERY_GET_PRODUCT_TRANSACTION_HISTORY = """
            SELECT 
                COUNT(*) as stockCount,  
                GROUP_CONCAT(DISTINCT CASE WHEN WE.firstNames IS NOT NULL THEN WE.firstNames ELSE '' END||' '||CASE WHEN WE.lastName IS NOT NULL THEN WE.lastName ELSE '' END) AS weNames,
                LE.firstNames||' '||LE.lastName as leName,
                LE.personUid AS fromLeUid,
                Sale.saleUid, TOLE.personUid as toLeUid,
                CASE WHEN Sale.saleUid THEN Sale.saleCreationDate ELSE InventoryItem.InventoryItemDateAdded END as transactionDate
            FROM InventoryTransaction
            LEFT JOIN InventoryItem ON InventoryTransaction.inventoryTransactionInventoryItemUid = InventoryItem.inventoryItemUid
            LEFT JOIN Product ON InventoryItem.InventoryItemProductUid = Product.productUid 
            LEFT JOIN Person as TOLE ON InventoryTransaction.InventoryTransactionToLeUid = TOLE.personUid 
            LEFT JOIN Person as LE ON InventoryTransaction.inventoryTransactionFromLeUid = LE.personUid
            LEFT JOIN Person as WE ON InventoryItem.InventoryItemWeUid = WE.personUid
            LEFT JOIN Sale ON InventoryTransaction.inventoryTransactionSaleUid = Sale.saleUid
            LEFT JOIN SaleItem ON InventoryTransaction.inventoryTransactionSaleItemUid = SaleItem.saleItemUid
            LEFT JOIN Person as MLE ON MLE.personUid = :leUid 
            WHERE
                CAST(InventoryTransaction.inventoryTransactionActive AS INTEGER) = 1  
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND 
                Product.productUid = :productUid 
                AND ( CASE WHEN (CAST(MLE.admin as INTEGER) = 0) THEN LE.personUid = MLE.personUid ELSE 1 END )
                AND CAST(InventoryTransaction.inventoryTransactionActive AS INTEGER) = 1
                AND 
                    (InventoryItem.inventoryItemWeUid IN (
                        SELECT MEMBER.personUid FROM PersonGroupMember 
                        LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
                        LEFT JOIN PERSON AS LE ON LE.personUid = MLE.personUid
                         WHERE groupMemberGroupUid = LE.personWeGroupUid 
                        AND CAST(groupMemberActive  AS INTEGER) = 1
                        ) 
                    OR 
                        CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END 
                    )
            GROUP BY saleUid, transactionDate
            ORDER BY transactionDate DESC
        """

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }



}
