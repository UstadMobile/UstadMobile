package com.ustadmobile.core.db.dao


import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductGroupJoin

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleProductGroupJoinDao : SyncableDao<SaleProductGroupJoin, SaleProductGroupJoinDao> {

    //INSERT

    @Insert
    abstract fun insertAsync(entity: SaleProductGroupJoin, insertCallback: UmCallback<Long>)

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): UmLiveData<List<SaleProductGroupJoin>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<SaleProductGroupJoin>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveAsync(allActiveCallback: UmCallback<List<SaleProductGroupJoin>>)

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): UmProvider<SaleProductGroupJoin>

    @Query("SELECT SaleProduct.* FROM SaleProductGroupJoin LEFT JOIN SaleProduct ON " +
            "SaleProductGroupJoin.saleProductGroupJoinProductUid = SaleProduct.saleProductUid " +
            "WHERE saleProductGroupJoinGroupUid = :collectionUid")
    abstract fun findListOfProductsInACollectionLive(collectionUid: Long): UmLiveData<List<SaleProduct>>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(uid: Long): SaleProductGroupJoin

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidAsync(uid: Long, findByUidCallback: UmCallback<SaleProductGroupJoin>)

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): UmLiveData<SaleProductGroupJoin>

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntity(uid: Long)

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntityAsync(uid: Long, inactivateCallback: UmCallback<Int>)


    //UPDATE:

    @Update
    abstract fun updateAsync(entity: SaleProductGroupJoin, updateCallback: UmCallback<Int>)

    companion object {


        //FIND ALL ACTIVE

        const val ALL_ACTIVE_QUERY = "SELECT * FROM SaleProductGroupJoin WHERE saleProductGroupJoinActive = 1"


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProductGroupJoin WHERE saleProductGroupJoinUid = :uid"

        //INACTIVATE:

        const val INACTIVATE_QUERY = "UPDATE SaleProductGroupJoin SET saleProductGroupJoinActive = 0 WHERE saleProductGroupJoinUid = :uid"
    }
}
