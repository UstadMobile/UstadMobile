package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.dao.SaleItemDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleItemDao : BaseDao<SaleItem> {

    @Update
    abstract suspend fun updateAsync(entity: SaleItem): Int


    @Query(QUERY_ALL_ACTIVE_SALE_ITEM_LIST)
    abstract fun findAllBySale(saleUid: Long): DataSource.Factory<Int,SaleItemWithProduct>


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
