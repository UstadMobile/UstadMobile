package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.lib.db.entities.PersonWithInventoryCount
import com.ustadmobile.lib.db.entities.PersonWithInventoryItemAndStock
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class InventoryItemDao : BaseDao<InventoryItem> {

    @Query(QUERY_GET_WE_NEW_INVENTORY)
    abstract suspend fun getAllWeWithNewInventoryItem(leUid: Long)
            : List<PersonWithInventoryItemAndStock>

    @Query(QUERY_GET_STOCK_LIST_BY_PRODUCT)
    abstract fun getStockListByProduct(productUid: Long, leUid: Long) :
            DataSource.Factory<Int, PersonWithInventoryCount>


    @Query(QUERY_GET_STOCK_LIST_BY_PRODUCT_AND_DELIVERY)
    abstract suspend fun getStockAndDeliveryListByProduct(productUid: Long, leUid: Long,
                                                  saleDeliveryUid: Long) :
            List<PersonWithInventoryItemAndStock>


    @Query(QUERY_GET_PRODUCT_TRANSACTION_HISTORY)
    abstract fun getProductTransactionDetail(productUid: Long, leUid: Long) :
            DataSource.Factory<Int, InventoryTransactionDetail>

    companion object{

        //TODO: Count existing stock
        const val QUERY_GET_WE_NEW_INVENTORY = """
        SELECT 
            Person.*,
            InventoryItem.*, 
            (0)  as stock , 
            0 as selectedStock
        FROM Person
        LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
        LEFT JOIN InventoryItem ON InventoryItem.inventoryItemUid = 0
        WHERE
            CAST(Person.admin AS INTEGER) = 0 AND
            CAST(Person.active AS INTEGER) = 1 AND
            (Person.personUid IN (
                SELECT MEMBER.personUid FROM PersonGroupMember 
                LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
                 WHERE groupMemberGroupUid = MLE.personWeGroupUid 
                AND CAST(groupMemberActive  AS INTEGER) = 1 ) 
                OR 
                CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END )
        """

        const val QUERY_GET_STOCK_LIST_BY_PRODUCT_AND_DELIVERY = """
            SELECT 
                Person.*,
                InventoryItem.*, 
                (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND InventoryItem.inventoryItemWeUid = Person.personUid
                AND InventoryItem.inventoryItemLeUid = :leUid
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                )  as stock , 
                (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND InventoryItem.inventoryItemWeUid = Person.personUid
                AND InventoryItem.inventoryItemLeUid = :leUid
                AND InventoryItem.inventoryItemSaleDeliveryUid = SaleDelivery.saleDeliveryUid
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                ) as selectedStock
            FROM Person
            LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
            LEFT JOIN InventoryItem ON InventoryItem.inventoryItemUid = 0
            LEFT JOIN Product ON Product.productUid = :productUid
            LEFT JOIN SaleDelivery ON SaleDelivery.saleDeliveryUid = :saleDeliveryUid
            WHERE
                CAST(Person.admin AS INTEGER) = 0 AND
                CAST(Person.active AS INTEGER) = 1 AND
                (Person.personUid IN (
                    SELECT MEMBER.personUid FROM PersonGroupMember 
                    LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
                     WHERE groupMemberGroupUid = MLE.personWeGroupUid 
                    AND CAST(groupMemberActive  AS INTEGER) = 1 ) 
                    OR 
                    CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END )
            
        """

        const val QUERY_GET_STOCK_LIST_BY_PRODUCT = """
            SELECT WE.*, 
            (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND InventoryItem.inventoryItemWeUid = WE.personUid
                AND InventoryItem.inventoryItemLeUid = :leUid
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
                SUM(inventoryItemQuantity) as stockCount,
                GROUP_CONCAT(DISTINCT CASE WHEN WE.firstNames IS NOT NULL 
                            THEN WE.firstNames ELSE '' END||' '|| CASE WHEN WE.lastName IS NOT NULL 
                                THEN WE.lastName ELSE '' END) AS weNames,
                LE.firstNames||' '||LE.lastName as leName,
                LE.personUid AS fromLeUid,
                inventoryItemSaleUid as saleUid, 
                inventoryItemDateAdded as transactionDate
            FROM InventoryItem
                LEFT JOIN Person AS LE ON LE.personUid = inventoryItemLeUid
                LEFT JOIN Person AS WE ON WE.personUid = inventoryItemWeUid
            WHERE 
                inventoryItemProductUid = :productUid 
                AND inventoryItemLeUid = :leUid
                AND CAST(inventoryItemActive AS INTEGER) = 1 
            GROUP BY inventoryItemDateAdded
        """



    }




}
