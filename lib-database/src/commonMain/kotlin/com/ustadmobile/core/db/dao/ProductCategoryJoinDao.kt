package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.db.entities.ProductCategoryJoin
import com.ustadmobile.door.annotation.Repository


@Repository
@Dao
abstract class ProductCategoryJoinDao : BaseDao<ProductCategoryJoin>,
        OneToManyJoinDao<ProductCategoryJoin>{

    @Query("""UPDATE ProductCategoryJoin SET productCategoryJoinActive = :active,
            productCategoryJoinLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE productCategoryJoinUid = :uid """)
    abstract suspend fun updateActiveByProductUid(uid: Long, active : Boolean)


    @Query("""UPDATE ProductCategoryJoin SET productCategoryJoinActive = :active,
            productCategoryJoinLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE productCategoryJoinProductUid = :productUid AND productCategoryJoinCategoryUid = :categoryUid """)
    abstract suspend fun updateActiveByProductAndCategoryUid(productUid: Long, categoryUid: Long, active : Boolean)

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByProductUid(it, false)
        }
    }

    suspend fun deactivateByCategoryAndProductUid(productUid: Long, categoryUids: List<Long>){
        categoryUids.forEach {
            updateActiveByProductAndCategoryUid(productUid, it, false)
        }
    }


    companion object {



    }
}
