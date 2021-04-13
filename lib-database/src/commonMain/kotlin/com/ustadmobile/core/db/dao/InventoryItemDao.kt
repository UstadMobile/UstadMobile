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
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class InventoryItemDao : BaseDao<InventoryItem> {

    @Query(QUERY_GET_WE_NEW_INVENTORY)
    abstract suspend fun getAllWeWithNewInventoryItem(leUid: Long, productUid: Long)
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

    @Query(QUERY_UPDATE_DELIVERY_ON_INVENTORY)
    abstract suspend fun updateSaleDeliveryOnInventoryItems(productUid: Long, leUid: Long,
                                                            weUid: Long,
                                                            saleDeliveryUid: Long,
                                                            saleUid: Long,
                                                            count: Long): Int

    companion object{

        const val QUERY_GET_WE_NEW_INVENTORY = """
        SELECT 
            Person.*,
            InventoryItem.*, 
            (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = :productUid
                AND InventoryItem.inventoryItemWeUid = Person.personUid
                AND (
                    InventoryItem.inventoryItemLeUid = MLE.personUid
                    OR
                    CAST(MLE.admin AS INTEGER) = 1
                    )
                
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
            )  as stock , 
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
                 WHERE 
                ( 
                    groupMemberGroupUid = MLE.personWeGroupUid
                    OR CAST(MEMBER.admin AS INTEGER) = 1   
                 )
                AND CAST(groupMemberActive  AS INTEGER) = 1 
                AND MEMBER.personGoldoziType = 2
                )
                OR 
				(CAST(MLE.admin AS INTEGER) = 1 AND Person.personGoldoziType = 2)
            ) 
                
        """

        const val QUERY_GET_STOCK_LIST_BY_PRODUCT_AND_DELIVERY = """
            SELECT 
                Person.*,
                InventoryItem.*, 
                (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND InventoryItem.inventoryItemWeUid = Person.personUid
                AND (
                    InventoryItem.inventoryItemLeUid = MLE.personUid
                    OR
                    CAST(MLE.admin AS INTEGER) = 1
                    )
                
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                )  as stock , 
                (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND InventoryItem.inventoryItemWeUid = Person.personUid
                AND ( 
                    InventoryItem.inventoryItemLeUid = :leUid
                    OR CAST(MLE.admin AS INTEGER) = 1 
                    )
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
                    AND CAST(groupMemberActive  AS INTEGER) = 1 
                    AND MEMBER.personGoldoziType = 2) 
                    
                     )
                     OR 
				(CAST(MLE.admin AS INTEGER) = 1 AND Person.personGoldoziType = 2)
                     
                     
            
        """

        const val QUERY_GET_STOCK_LIST_BY_PRODUCT = """
            SELECT WE.*, 
            (
                SELECT SUM(inventoryItemQuantity) FROM InventoryItem WHERE      
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND InventoryItem.inventoryItemWeUid = WE.personUid
                AND (
                    InventoryItem.inventoryItemLeUid = LE.personUid
                    OR
                    CAST(LE.admin AS INTEGER) = 1
                    )
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
            )  as inventoryCount, 
            0 as inventoryCountDeliveredTotal, 
            0 as inventoryCountDelivered, 
            0 as inventorySelected
            FROM PersonGroupMember
            LEFT JOIN Person AS WE ON WE.personUid = PersonGroupMember.groupMemberPersonUid
             LEFT JOIN Person AS LE ON LE.personUid = :leUid
            LEFT JOIN Product ON Product.productUid = :productUid
            LEFT JOIN Person AS PLE ON PLE.personUid = Product.productPersonAdded
            WHERE 
            CAST(LE.active AS INTEGER) = 1 
            AND ( 
            PersonGroupMember.groupMemberGroupUid = LE.personWeGroupUid
            OR CASE WHEN  
                CAST(LE.admin AS INTEGER) = 1 THEN PersonGroupMember.groupMemberGroupUid = PLE.personWeGroupUid ELSE 0 END
            )
             AND WE.personGoldoziType = ${Person.GOLDOZI_TYPE_PRODUCER}
            
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
                LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
            WHERE 
                inventoryItemProductUid = :productUid 
                AND ( inventoryItemLeUid = MLE.personUid
                OR CAST(MLE.admin AS INTEGER) = 1
                )
                
                AND CAST(inventoryItemActive AS INTEGER) = 1 
            GROUP BY inventoryItemDateAdded
        """


        /**
         * productUid: Long, leUid: Long,
        saleDeliveryUid: Long,
        saleUid: Long
         */
        const val QUERY_UPDATE_DELIVERY_ON_INVENTORY = """

            UPDATE InventoryItem SET 
                inventoryItemSaleDeliveryUid = :saleDeliveryUid, 
                inventoryItemSaleUid = :saleUid
            WHERE inventoryItemUid IN 
			(SELECT II.inventoryItemUid FROM InventoryItem AS II
				WHERE II.inventoryItemLeUid = :leUid
                AND II.inventoryItemProductUid = :productUid
                AND II.inventoryItemWeUid = :weUid 
            ORDER BY II.inventoryItemDateAdded ASC LIMIT :count)


        """


    }




}
