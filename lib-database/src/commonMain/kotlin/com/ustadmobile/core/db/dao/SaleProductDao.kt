package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleProduct

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleProductDao : BaseDao<SaleProduct> {

    //INSERT

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): DoorLiveData<List<SaleProduct>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<SaleProduct>

    @Query(ALL_ACTIVE_QUERY)
    abstract suspend fun findAllActiveAsync() : List<SaleProduct>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): DataSource.Factory<Int,SaleProduct>

    //FIND ALL CATEGORIES

    @Query(ALL_ACTIVE_CATEGORY_QUERY)
    abstract suspend fun findAllCategoriesAsync() : List<SaleProduct>

    @Query(ALL_ACTIVE_CATEGORY_QUERY)
    abstract fun findAllCateogoriesProvider(): DataSource.Factory<Int,SaleProduct>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveSNWILive(): DoorLiveData<List<SaleProduct>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveSNWIList(): List<SaleProduct>

    @Query(ALL_ACTIVE_QUERY)
    abstract suspend fun findAllActiveSNWIAsync() : List<SaleProduct>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveSNWIProvider(): DataSource.Factory<Int,SaleProduct>

    //ITEMS(PRODUCTS):
    @Query(ACTIVE_PRODUCTS_QUERY)
    abstract fun findAllActiveProductsSNWILive(): DoorLiveData<List<SaleProduct>>

    @Query(ACTIVE_PRODUCTS_QUERY)
    abstract fun findAllActiveProductsSNWIList(): List<SaleProduct>

    @Query(ACTIVE_PRODUCTS_QUERY)
    abstract suspend fun findAllActiveProductsSNWIAsync():List<SaleProduct>

    @Query(ACTIVE_PRODUCTS_QUERY)
    abstract fun findActiveProductsProvider(): DataSource.Factory<Int,SaleProduct>

    //Products not existing already in Category
    @Query(ACTIVE_PRODUCTS_NOT_IN_CATEGORY_QUERY)
    abstract fun findAllActiveProductsNotInCategorySNWIProvider(
            saleProductCategoryUid: Long): DataSource.Factory<Int,SaleProduct>

    //CATEGORY:
    @Query(ACTIVE_CATEGORIES_QUERY)
    abstract fun findAllActiveCategoriesSNWILive(): DoorLiveData<List<SaleProduct>>

    @Query(ACTIVE_CATEGORIES_QUERY)
    abstract fun findAllActiveCategoriesSNWIList(): List<SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY)
    abstract suspend fun findAllActiveCategoriesSNWIAsync():List<SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY)
    abstract fun findActiveCategoriesProvider(): DataSource.Factory<Int,SaleProduct>

    //Categories not existing already in Category
    @Query(ACTIVE_CATEGORIES_NOT_IN_CATEGORY_QUERY)
    abstract fun findAllActiveCategoriesNotInCategorySNWIProvider(
            saleProductCategoryUid: Long
    ): DataSource.Factory<Int, SaleProduct>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(uid: Long): SaleProduct?

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(uid: Long): SaleProduct?

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): DoorLiveData<SaleProduct?>

    @Query(FIND_BY_NAME_QUERY)
    abstract fun findByName(name: String): SaleProduct?

    @Query(FIND_BY_NAME_QUERY)
    abstract suspend fun findByNameAsync(name: String):SaleProduct?

    @Query(FIND_BY_NAME_QUERY)
    abstract fun findByNameLive(name: String): DoorLiveData<SaleProduct?>

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntity(uid: Long)

    @Query(INACTIVATE_QUERY)
    abstract suspend fun inactivateEntityAsync(uid: Long):Int

    //UPDATE:

    @Update
    abstract suspend fun updateAsync(entity: SaleProduct):Int

    @Query("select group_concat(saleProductName, ', ') from SaleProduct " +
            " WHERE SaleProductUid in (:uids)")
    abstract suspend fun findAllProductNamesInUidList(uids:List<Long>):String?


    companion object {

        const val ALL_ACTIVE_QUERY = "SELECT SaleProduct.* FROM SaleProduct " +
                " WHERE saleProductActive = 1 "

        const val AND_IS_CATEGORY = " AND SaleProduct.saleProductCategory = 1 "

        const val AND_IS_NOT_CATEGORY = " AND SaleProduct.saleProductCategory = 0 "

        const val AND_NOT_IN_CATEGORY =
                " AND SaleProduct.saleProductUid NOT IN (Select SaleProduct.saleProductUid FROM SaleProductParentJoin " +
                " LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid " +
                " WHERE SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
                " AND SaleProductParentJoin.saleProductParentJoinActive = 1 AND SaleProduct.saleProductActive = 1) " +
                " AND SaleProduct.saleProductUid != :saleProductCategoryUid "

        const val ALL_ACTIVE_CATEGORY_QUERY = ALL_ACTIVE_QUERY + AND_IS_CATEGORY

        const  val ACTIVE_PRODUCTS_QUERY = ALL_ACTIVE_QUERY + AND_IS_NOT_CATEGORY

        const val ACTIVE_CATEGORIES_QUERY = ALL_ACTIVE_QUERY + AND_IS_CATEGORY

        const val ACTIVE_PRODUCTS_NOT_IN_CATEGORY_QUERY =
                ALL_ACTIVE_QUERY + AND_IS_NOT_CATEGORY + AND_NOT_IN_CATEGORY

        const val ACTIVE_CATEGORIES_NOT_IN_CATEGORY_QUERY =
                ALL_ACTIVE_QUERY + AND_IS_CATEGORY + AND_NOT_IN_CATEGORY


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProduct WHERE saleProductUid = :uid"

        const val FIND_BY_NAME_QUERY = "SELECT * FROM SaleProduct WHERE saleProductName = :name AND saleProductActive = 1"

        //INACTIVATE:

        const val INACTIVATE_QUERY = "UPDATE SaleProduct SET saleProductActive = 0 WHERE saleProductUid = :uid"
    }



}
