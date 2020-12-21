package com.ustadmobile.core.db.dao


import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class SaleItemDao : BaseDao<SaleItem>, OneToManyJoinDao<SaleItem> {


    @Query("""UPDATE SaleItem SET saleItemSaleUid = 0
        WHERE saleItemUid = :saleItemUid
    """)
    abstract suspend fun deactivateSaleFromSaleItem(saleItemUid : Long ): Int

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            deactivateSaleFromSaleItem(it)
        }
    }

    @Update
    abstract suspend fun updateAsync(entity: SaleItem): Int

    @Query(QUERY_ALL_ACTIVE_SALE_ITEM_LIST)
    abstract fun findAllBySale(saleUid: Long): DataSource.Factory<Int,SaleItemWithProduct>

    @Query(QUERY_ALL_ACTIVE_SALE_ITEM_LIST)
    abstract suspend fun findAllBySaleListAsList(saleUid: Long): List<SaleItemWithProduct>

    @Query("""
            SELECT SUM(saleItemPricePerPiece * saleItemQuantity) FROM SaleItem
            WHERE saleItemSaleUid = :saleUid AND CAST(saleItemActive AS INTEGER) = 1 """)
    abstract fun findTotalBySaleLive(saleUid: Long):DoorLiveData<Long>

    @Query("""
            SELECT CASE WHEN SUM(saleItemPricePerPiece * saleItemQuantity) 
                THEN SUM(saleItemPricePerPiece * saleItemQuantity) ELSE 0 END 
             FROM SaleItem 
            WHERE saleItemSaleUid = :saleUid AND CAST(saleItemActive AS INTEGER) = 1 """)
    abstract suspend fun findTotalBySale(saleUid: Long): Long


    @Query(QUERY_FIND_WITH_PRODUCT_BY_UID)
    abstract suspend fun findWithProductByUidAsync(uid: Long, leUid: Long): SaleItemWithProduct?

    @Query("SELECT * FROM SaleItem WHERE CAST(saleItemActive AS INTEGER) = 1 ")
    abstract fun findAllActiveLive(): DoorLiveData<List<SaleItem>>

    @Query(QUERY_FIND_WITH_PRODUCT_BY_UID)
    abstract fun findWithProductByUidLive(uid: Long, leUid: Long): DoorLiveData<SaleItemWithProduct?>

    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

        const val QUERY_FIND_WITH_PRODUCT_BY_UID = """
            SELECT SaleItem.* , Product.*, 
            ( 
            SELECT 
                    CASE WHEN CAST(SUM(InventoryItem.inventoryItemQuantity) AS INTEGER) > 0 
                        THEN SUM(InventoryItem.inventoryItemQuantity) 
                        ELSE 0 
                    END
                FROM InventoryItem WHERE
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND (CAST(LE.admin AS INTEGER) = 1 OR InventoryItem.inventoryItemLeUid = LE.personUid)
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
            ) as deliveredCount 
            FROM SaleItem 
            LEFT JOIN Product ON Product.productUid = SaleItem.saleItemProductUid
            LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
            WHERE SaleItem.saleItemUid = :uid AND CAST(SaleItem.saleItemActive AS INTEGER ) = 1
        """
        const val QUERY_ALL_ACTIVE_SALE_ITEM_LIST =
                """ 
                    SELECT SaleItem.*, 
                        Product.* , 
                        (SELECT count(*) FROM inventorytransaction WHERE 
                            inventorytransactionsaleitemuid = SaleItem.saleItemUid AND 
                            CAST(inventorytransactionactive AS INTEGER) = 1 
                            AND inventoryTransactionSaleDeliveryUid != 0 ) as deliveredCount
                    FROM SaleItem 
                        LEFT JOIN Product ON SaleItem.saleItemProductUid = Product.productUid 
                       
                    WHERE 
                        CAST(saleItemActive AS INTEGER) = 1 AND SaleItem.saleItemSaleUid = :saleUid 
                """
    }
}
