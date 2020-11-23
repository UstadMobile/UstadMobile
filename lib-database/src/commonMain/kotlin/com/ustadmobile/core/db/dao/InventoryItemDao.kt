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
import com.ustadmobile.lib.db.entities.InventoryTransaction
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class InventoryItemDao : BaseDao<InventoryItem> {


    @Query(QUERY_BY_PERSON)
    abstract suspend fun findWeStock(productUid: Long, leUid: Long)
            : List<PersonWithInventory>


    @Insert
    abstract suspend fun insertTransaction(inventoryTransaction: InventoryTransaction)

    suspend fun insertInventoryItem(item: InventoryItem, count: Int, leUid:Long){

        var x =0
        while(x < count){

            val inventoryItemUid = insert(item)
            val inventoryTransaction = InventoryTransaction()
            inventoryTransaction.inventoryTransactionInventoryItemUid = inventoryItemUid
            inventoryTransaction.inventoryTransactionFromLeUid = leUid
            insertTransaction(inventoryTransaction)

            x++;
        }
    }


    companion object{


        const val QUERY_INVENTORY_LIST_SORTBY_NAME_ASC =
                " ORDER BY Product.productName ASC "


        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

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
                                inventoryitem.inventoryitemproductuid = Product.productUid
                                AND InventoryItem.inventoryItemWeUid = Person.personUid
                                AND InventoryItem.inventoryItemLeUid = :leUid
                                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                            ) 
                            and inventorytransaction.inventoryTransactionSaleUid != 0 
                            and cast(inventorytransaction.inventoryTransactionActive AS INTEGER) = 1 )
                    ) 
                FROM InventoryItem
                    LEFT JOIN Product ON Product.productUid = InventoryItemProductUid
                WHERE
                    CAST(Product.productActive AS INTEGER) = 1 AND
                    CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 AND
                    Product.productUid = :productUid 
                    AND InventoryItem.inventoryItemWeUid = Person.personUid
                    AND InventoryItem.inventoryItemLeUid = :leUid
                    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
            ) as inventoryCount, 
            -1 as inventorySelected,
            -1 as inventoryCountDeliveredTotal, 
            -1 as inventoryCountDelivered
        FROM Person
        LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
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

    }




}
