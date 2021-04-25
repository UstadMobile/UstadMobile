package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class SaleDeliveryDao : BaseDao<SaleDelivery>, OneToManyJoinDao<SaleDelivery> {

    @Update
    abstract suspend fun updateAsync(entity: SaleDelivery): Int

    @Query(QUERY_ALL_ACTIVE_SALE_DELIVERY_LIST)
    abstract fun findAllBySale(saleUid: Long): DataSource.Factory<Int,SaleDelivery>

    @Query(QUERY_ALL_ACTIVE_SALE_DELIVERY_LIST)
    abstract suspend fun findAllBySaleAsList(saleUid: Long): List<SaleDelivery>

    @Query("""
        SELECT Product.* FROM InventoryItem
        LEFt JOIN Product ON Product.productUid = InventoryItem.inventoryItemProductUid
         WHERE InventoryItem.inventoryItemSaleDeliveryUid = :saleDeliveryUid
         AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
    """)
    abstract suspend fun findAllProductsInThisDelivery(saleDeliveryUid: Long): List<Product>

    @Query("""
        SELECT * FROM SaleDelivery WHERE saleDeliveryUid = :uid 
        AND CAST(saleDeliveryActive AS INTEGER) = 1
    """)
    abstract suspend fun findByUidAsync(uid: Long): SaleDelivery?

    @Query("""
        SELECT SaleItem.*, Product.*, 0 as deliveredCount FROM SaleItem 
        LEFT JOIN Product ON Product.productUid = SaleItem.saleItemProductUid
        WHERE SaleItem.saleItemSaleUid = :saleUid 
        
        
    """)
    abstract suspend fun findAllSaleItemsByDelivery(saleUid: Long):
            List<SaleItemWithProduct>


    @Query("""
        UPDATE SaleDelivery SET saleDeliverySaleUid = 0 
        WHERE saleDeliveryUid = :saleDeliveryUid
    """)
    abstract suspend fun deactivateSaleFromSaleDelivery(saleDeliveryUid: Long): Int

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            deactivateSaleFromSaleDelivery(it)
        }
    }

    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"


        const val QUERY_ALL_ACTIVE_SALE_DELIVERY_LIST =
                """ 
                    SELECT SaleDelivery.* FROM SaleDelivery 
                    WHERE SaleDelivery.saleDeliverySaleUid = :saleUid 
                        AND CAST(SaleDelivery.saleDeliveryActive AS INTEGER) = 1
                    
                """
    }
}
