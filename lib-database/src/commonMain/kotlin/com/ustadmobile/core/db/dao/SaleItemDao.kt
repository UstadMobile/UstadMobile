package com.ustadmobile.core.db.dao


import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*


@UmRepository
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
            SELECT SUM(saleItemPricePerPiece * saleItemQuantity) FROM SaleItem 
            WHERE saleItemSaleUid = :saleUid AND CAST(saleItemActive AS INTEGER) = 1 """)
    abstract fun findTotalBySale(saleUid: Long): Long

    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

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
