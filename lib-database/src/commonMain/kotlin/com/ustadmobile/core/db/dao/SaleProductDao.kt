package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_DESC

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleProduct

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleProductDao : BaseDao<SaleProduct> {

    //FIND ALL CATEGORIES

    @Query(ALL_ACTIVE_QUERY + SORT_BY_NAME_ASC_QUERY)
    abstract fun findAllActiveSNWIProviderByNameAsc(): DataSource.Factory<Int,SaleProduct>

    @Query(ALL_ACTIVE_QUERY + SORT_BY_NAME_DESC_QUERY)
    abstract fun findAllActiveSNWIProviderByNameDesc(): DataSource.Factory<Int,SaleProduct>

    fun sortAndFindAllActiveSNWIProvider(sortCode: Int): DataSource.Factory<Int, SaleProduct>{
        when(sortCode){
            SaleDao.SORT_ORDER_NAME_ASC -> return findAllActiveSNWIProviderByNameAsc()
            SaleDao.SORT_ORDER_NAME_DESC -> return findAllActiveSNWIProviderByNameDesc()
        }
        return findAllActiveSNWIProviderByNameAsc()

    }
    //ITEMS(PRODUCTS):

    @Query(ACTIVE_PRODUCTS_QUERY)
    abstract fun findActiveProductsProvider(query: String): DataSource.Factory<Int,SaleProduct>

    //Products not existing already in Category
    @Query(ACTIVE_PRODUCTS_NOT_IN_CATEGORY_QUERY)
    abstract fun findAllActiveProductsNotInCategorySNWIProvider(
            saleProductCategoryUid: Long): DataSource.Factory<Int,SaleProduct>

    //CATEGORY:


    @Query(ACTIVE_CATEGORIES_QUERY +  SORT_BY_NAME_ASC_QUERY)
    abstract fun findActiveCategoriesProviderByNameAsc(query: String): DataSource.Factory<Int,SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY + SORT_BY_NAME_DESC_QUERY)
    abstract fun findActiveCategoriesProviderByNameDesc(query: String): DataSource.Factory<Int,SaleProduct>

    fun sortAndFindActiveCategoriesProvider(query: String, sortCode: Int): DataSource.Factory<Int, SaleProduct>{
        when(sortCode){
            SORT_ORDER_NAME_ASC -> return findActiveCategoriesProviderByNameAsc(query)
            SORT_ORDER_NAME_DESC -> return findActiveCategoriesProviderByNameDesc(query)
        }
        return findActiveCategoriesProviderByNameAsc(query)
    }

    //Categories not existing already in Category
    @Query(ACTIVE_CATEGORIES_NOT_IN_CATEGORY_QUERY)
    abstract fun findAllActiveCategoriesNotInCategorySNWIProvider(
            saleProductCategoryUid: Long
    ): DataSource.Factory<Int, SaleProduct>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): DoorLiveData<SaleProduct?>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidAsync(uid: Long): SaleProduct?

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
                " WHERE CAST(saleProductActive AS INTEGER) = 1 "

        const val ALL_ACTIVE_QUERY_WITH_SEARCH = " AND SaleProduct.saleProductName LIKE :query "

        const val SORT_BY_NAME_ASC_QUERY = " ORDER BY SaleProduct.saleProductName ASC "

        const val SORT_BY_NAME_DESC_QUERY = " ORDER BY SaleProduct.saleProductName DESC "

        const val AND_IS_CATEGORY = " AND CAST(SaleProduct.saleProductCategory AS INTEGER) = 1 "

        const val AND_IS_NOT_CATEGORY = " AND CAST(SaleProduct.saleProductCategory AS INTEGER) = 0 "

        const val AND_NOT_IN_CATEGORY =
                " AND SaleProduct.saleProductUid " +
                " NOT IN (Select SaleProduct.saleProductUid FROM SaleProductParentJoin " +
                "   LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid " +
                "   WHERE SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
                "   AND CAST(SaleProductParentJoin.saleProductParentJoinActive AS INTEGER) = 1" +
                "   AND CAST(SaleProduct.saleProductActive AS INTEGER) = 1) " +
                " AND SaleProduct.saleProductUid != :saleProductCategoryUid "

        const  val ACTIVE_PRODUCTS_QUERY = ALL_ACTIVE_QUERY + ALL_ACTIVE_QUERY_WITH_SEARCH +
                AND_IS_NOT_CATEGORY

        const val ACTIVE_CATEGORIES_QUERY = ALL_ACTIVE_QUERY + ALL_ACTIVE_QUERY_WITH_SEARCH +
                AND_IS_CATEGORY

        const val ACTIVE_PRODUCTS_NOT_IN_CATEGORY_QUERY =
                ALL_ACTIVE_QUERY + AND_IS_NOT_CATEGORY + AND_NOT_IN_CATEGORY

        const val ACTIVE_CATEGORIES_NOT_IN_CATEGORY_QUERY =
                ALL_ACTIVE_QUERY + AND_IS_CATEGORY + AND_NOT_IN_CATEGORY


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProduct WHERE saleProductUid = :uid"

        const val FIND_BY_NAME_QUERY = "SELECT * FROM SaleProduct WHERE saleProductName = :name AND CAST(saleProductActive AS INTEGER) = 1"

        //INACTIVATE:
        //TODO: Replace with Boolean argument
        const val INACTIVATE_QUERY = "UPDATE SaleProduct SET saleProductActive = 0 WHERE saleProductUid = :uid"
    }



}
