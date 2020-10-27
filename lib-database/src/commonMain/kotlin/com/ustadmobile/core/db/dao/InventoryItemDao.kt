package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryItem
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class InventoryItemDao : BaseDao<InventoryItem> {

    @Query(QUERY_INVENTORY_LIST + QUERY_INVENTORY_LIST_SORTBY_NAME_ASC)
    abstract fun findAllInventoryByProductNameAsc(leUid: Long, searchBit: String)
            : DataSource.Factory<Int, ProductWithInventoryCount>


    @Query(QUERY_PRODUCTS_WITH_INVENTORY)
    abstract fun findAllProductsWithInventoryCount():
            DataSource.Factory<Int, ProductWithInventoryCount>

    companion object{


        const val QUERY_PRODUCTS_WITH_INVENTORY = """
            SELECT Product.*, 
                (0) as stock
            FROM Product 
            WHERE CAST(productActive AS INTEGER) = 1 
        """


        const val QUERY_INVENTORY_LIST = """
            SELECT Product.*, 
                COUNT(*) - 
                    (select count(*) from inventorytransaction 
                    left join inventoryitem as item on item.inventoryitemuid = inventorytransaction.inventorytransactioninventoryitemuid
                    where 
                    inventorytransaction.inventoryTransactionInventoryItemUid in 
                    (select inventoryitemuid from inventoryitem where 
                    inventoryitem.inventoryitemsaleproductuid = Product.productUid
                    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 ) 
                    and inventorytransaction.inventoryTransactionSaleUid != 0 
                    and cast(inventorytransaction.inventorytransactionactive as integer) = 1 
                    and item.inventoryItemLeUid = :leUid)   
                as stock 
                FROM InventoryItem 
                LEFT JOIN Product ON Product.productUid = InventoryItemProductUid
                LEFT JOIN PERSON AS MLE ON MLE.personUid = :leUid
                WHERE CAST(Product.productActive AS INTEGER) = 1 
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                AND productName LIKE :searchBit
            
                AND (InventoryItem.InventoryItemWeUid IN (
                SELECT MEMBER.personUid FROM PersonGroupMember 
                LEFT JOIN PERSON AS MEMBER ON MEMBER.personUid = PersonGroupMember.groupMemberPersonUid
                LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
                 WHERE groupMemberGroupUid = LE.mPersonGroupUid 
                AND CAST(groupMemberActive  AS INTEGER) = 1
                ) OR CAST(CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END AS INTEGER) = 1 )
                AND (InventoryItem.inventoryItemLeUid = :leUid OR CAST(CASE WHEN (CAST(MLE.admin as INTEGER) = 1) THEN 1 ELSE 0 END AS INTEGER) = 1 )
            
                GROUP BY Product.productUid
        """


        const val QUERY_INVENTORY_LIST_SORTBY_NAME_ASC =
                " ORDER BY Product.productName ASC "



        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }



}
