package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.db.dao.InventoryItemDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.InventoryTransaction
import com.ustadmobile.lib.db.entities.InventoryTransactionDetail
import com.ustadmobile.lib.db.entities.PersonWithInventoryCount
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class InventoryTransactionDao : BaseDao<InventoryTransaction> {

    @Query(QUERY_GET_STOCK_LIST_BY_PRODUCT)
    abstract fun getStockListByProduct(productUid: Long) :
            DataSource.Factory<Int, PersonWithInventoryCount>

    @Query(QUERY_GET_TRANSACTION_LIST_BY_PRODUCT)
    abstract fun getProductTransactionDetail(productUid: Long) :
            DataSource.Factory<Int, InventoryTransactionDetail>

    companion object{


        const val QUERY_INVENTORY_LIST_SORTBY_NAME_ASC =
                " ORDER BY Product.productName ASC "

        const val QUERY_GET_STOCK_LIST_BY_PRODUCT = """
            SELECT Person.*, 
            0 as inventoryCount, 
            0 as inventoryCountDeliveredTotal, 
            0 as inventoryCountDelivered, 
            0 as inventorySelected
            FROM Person 
            LEFT JOIN Product ON Product.productUid = :productUid
            WHERE 
            CAST(Person.active AS INTEGER) = 1 
        """

        const val QUERY_GET_TRANSACTION_LIST_BY_PRODUCT = """
           SELECT 
            0 as stockCount, 
            '' as weNames, 
            0 as toLeUid, 
            0 as transactionDate, 
            '' as leName, 
            0 as fromLeUid 
            FROM InventoryTransaction
             LEFT JOIN Product ON Product.productUid = :productUid
        """

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }



}
