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


    companion object{


        const val QUERY_INVENTORY_LIST_SORTBY_NAME_ASC =
                " ORDER BY Product.productName ASC "


        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }



}
