package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.db.dao.ProductDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ProductCategoryJoin
import com.ustadmobile.door.annotation.Repository


@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@Repository
@Dao
abstract class ProductCategoryJoinDao : BaseDao<ProductCategoryJoin>,
        OneToManyJoinDao<ProductCategoryJoin>{

    @Query("""UPDATE ProductCategoryJoin SET productCategoryJoinActive = :active,
            productCategoryJoinLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE productCategoryJoinUid = :uid """)
    abstract suspend fun updateActiveByProductUid(uid: Long, active : Boolean)

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByProductUid(it, false)
        }
    }


    companion object {



    }
}
