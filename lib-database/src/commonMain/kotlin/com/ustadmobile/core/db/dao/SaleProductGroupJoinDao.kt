package com.ustadmobile.core.db.dao


import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductGroupJoin

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleProductGroupJoinDao : BaseDao<SaleProductGroupJoin> {

    //INSERT

    @Insert
    abstract override suspend fun insertAsync(entity: SaleProductGroupJoin):Long

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): DoorLiveData<List<SaleProductGroupJoin>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<SaleProductGroupJoin>

    @Query(ALL_ACTIVE_QUERY)
    abstract suspend fun findAllActiveAsync():List<SaleProductGroupJoin>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): DataSource.Factory<Int, SaleProductGroupJoin>

    @Query("SELECT SaleProduct.* FROM SaleProductGroupJoin LEFT JOIN SaleProduct ON " +
            "SaleProductGroupJoin.saleProductGroupJoinProductUid = SaleProduct.saleProductUid " +
            "WHERE saleProductGroupJoinGroupUid = :collectionUid")
    abstract fun findListOfProductsInACollectionLive(collectionUid: Long): DoorLiveData<List<SaleProduct>>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(uid: Long): SaleProductGroupJoin

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(uid: Long):SaleProductGroupJoin

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): DoorLiveData<SaleProductGroupJoin>

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntity(uid: Long)

    @Query(INACTIVATE_QUERY)
    abstract suspend fun inactivateEntityAsync(uid: Long):Int


    //UPDATE:

    @Update
    abstract suspend fun updateAsync(entity: SaleProductGroupJoin): Int

    companion object {


        //FIND ALL ACTIVE

        const val ALL_ACTIVE_QUERY = "SELECT * FROM SaleProductGroupJoin WHERE saleProductGroupJoinActive = 1"


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProductGroupJoin WHERE saleProductGroupJoinUid = :uid"

        //INACTIVATE:

        const val INACTIVATE_QUERY = "UPDATE SaleProductGroupJoin SET saleProductGroupJoinActive = 0 WHERE saleProductGroupJoinUid = :uid"
    }



}
