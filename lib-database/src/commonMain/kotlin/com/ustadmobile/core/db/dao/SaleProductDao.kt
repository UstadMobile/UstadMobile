package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_DESC

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.SyncNode
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleProduct

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleProductDao : BaseDao<SaleProduct> {

    //FIND ALL CATEGORIES

    @Query(ALL_ACTIVE_QUERY_WITH_LE_FILTER + ALL_ACTIVE_QUERY_WITH_SEARCH + SORT_BY_NAME_ENG_ASC_QUERY)
    abstract fun findAllActiveSNWIProviderByNameAsc(leUid: Long, query: String): DataSource.Factory<Int,SaleProduct>

    @Query(ALL_ACTIVE_QUERY_WITH_LE_FILTER + ALL_ACTIVE_QUERY_WITH_SEARCH + SORT_BY_NAME_DESC_QUERY)
    abstract fun findAllActiveSNWIProviderByNameDesc(leUid: Long, query: String): DataSource.Factory<Int,SaleProduct>

    fun sortAndFindAllActiveSNWIProvider(leUid: Long, sortCode: Int, query: String): DataSource.Factory<Int, SaleProduct>{
        when(sortCode){
            SORT_ORDER_NAME_ASC -> return findAllActiveSNWIProviderByNameAsc(leUid, query)
            SORT_ORDER_NAME_DESC -> return findAllActiveSNWIProviderByNameDesc(leUid, query)
        }
        return findAllActiveSNWIProviderByNameAsc(leUid, query)

    }
    //ITEMS(PRODUCTS):

    @Query(ACTIVE_PRODUCTS_QUERY)
    abstract fun findActiveProductsProvider(leUid: Long, query: String): DataSource.Factory<Int,SaleProduct>

    //Products not existing already in Category
    @Query(ACTIVE_PRODUCTS_NOT_IN_CATEGORY_QUERY)
    abstract fun findAllActiveProductsNotInCategorySNWIProvider(leUid: Long,
            saleProductCategoryUid: Long): DataSource.Factory<Int,SaleProduct>

    //CATEGORY:


    @Query(ACTIVE_CATEGORIES_QUERY +  SORT_BY_NAME_ENG_ASC_QUERY)
    abstract fun findActiveCategoriesProviderByNameAscEng(leUid: Long, query: String)
            : DataSource.Factory<Int,SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY +  SORT_BY_NAME_DARI_ASC_QUERY)
    abstract fun findActiveCategoriesProviderByNameAscDari(leUid: Long, query: String)
            : DataSource.Factory<Int,SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY + SORT_BY_NAME_PASHTO_ASC_QUERY )
    abstract fun findActiveCategoriesProviderByNameAscPashto(leUid: Long, query: String)
            : DataSource.Factory<Int,SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY + SORT_BY_NAME_ENG_DESC_QUERY)
    abstract fun findActiveCategoriesProviderByNameDescEng(leUid: Long, query: String)
            : DataSource.Factory<Int,SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY + SORT_BY_NAME_DARI_DESC_QUERY)
    abstract fun findActiveCategoriesProviderByNameDescDari(leUid: Long, query: String)
            : DataSource.Factory<Int,SaleProduct>

    @Query(ACTIVE_CATEGORIES_QUERY + SORT_BY_NAME_PASHTO_DESC_QUERY)
    abstract fun findActiveCategoriesProviderByNameDescPashto(leUid: Long, query: String)
            : DataSource.Factory<Int,SaleProduct>

    fun sortAndFindActiveCategoriesProvider(leUid: Long, query: String, sortCode: Int, locale: String)
            : DataSource.Factory<Int, SaleProduct>{
        when(sortCode){
            SORT_ORDER_NAME_ASC -> {
                if(locale.equals("ps")){
                    return findActiveCategoriesProviderByNameAscPashto(leUid, query)
                }else if(locale.equals("fa")){
                    return findActiveCategoriesProviderByNameAscDari(leUid, query)
                }else{
                    return findActiveCategoriesProviderByNameAscEng(leUid, query)
                }
            }
            SORT_ORDER_NAME_DESC -> {
                if(locale.equals("ps")){
                    return findActiveCategoriesProviderByNameDescPashto(leUid, query)
                }else if(locale.equals("fa")){
                    return findActiveCategoriesProviderByNameDescDari(leUid, query)
                }else{
                    return findActiveCategoriesProviderByNameDescEng(leUid, query)
                }
            }
        }
        if(locale.equals("ps")){
            return findActiveCategoriesProviderByNameAscPashto(leUid, query)
        }else if(locale.equals("fa")){
            return findActiveCategoriesProviderByNameAscDari(leUid, query)
        }else{
            return findActiveCategoriesProviderByNameAscEng(leUid, query)
        }

    }

    //Categories not existing already in Category
    @Query(ACTIVE_CATEGORIES_NOT_IN_CATEGORY_QUERY)
    abstract fun findAllActiveCategoriesNotInCategorySNWIProvider(leUid: Long,
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


        const val ALL_ACTIVE_QUERY_WITH_LE_FILTER = """
            SELECT SaleProduct.* FROM SaleProduct 
            LEFT JOIN Person AS LE ON LE.personUid = :leUid
            WHERE CAST(saleProductActive AS INTEGER) = 1 
            AND (CAST(LE.admin AS INTEGER) = 1 OR SaleProduct.saleProductPersonAdded = LE.personUid )
        """

        const val ALL_ACTIVE_QUERY_WITHOUT_LE_FILTER = """
            SELECT SaleProduct.* FROM SaleProduct 
            LEFT JOIN Person AS LE ON LE.personUid = :leUid
            WHERE CAST(saleProductActive AS INTEGER) = 1
        """

        const val ALL_ACTIVE_QUERY_WITH_SEARCH =
                    """ AND (lower(SaleProduct.saleProductName) like :query OR
                     lower(SaleProduct.saleProductNameDari) like :query OR 
                    lower(SaleProduct.saleProductNamePashto) like :query )"""

        const val SORT_BY_NAME_ENG_ASC_QUERY = " ORDER BY SaleProduct.saleProductName ASC "
        const val SORT_BY_NAME_DARI_ASC_QUERY = " ORDER BY SaleProduct.saleProductNameDari ASC "
        const val SORT_BY_NAME_PASHTO_ASC_QUERY = " ORDER BY SaleProduct.saleProductNamePashto ASC "

        const val SORT_BY_NAME_ENG_DESC_QUERY = " ORDER BY SaleProduct.saleProductName DESC "
        const val SORT_BY_NAME_DARI_DESC_QUERY = " ORDER BY SaleProduct.saleProductNameDari DESC "
        const val SORT_BY_NAME_PASHTO_DESC_QUERY = " ORDER BY SaleProduct.saleProductNamePashto DESC "



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

        const val AND_NOT_COLLECTIONS =
                """
                    AND SaleProduct.saleProductName != 'Collections'
                """

        const  val ACTIVE_PRODUCTS_QUERY = ALL_ACTIVE_QUERY_WITH_LE_FILTER + ALL_ACTIVE_QUERY_WITH_SEARCH +
                AND_IS_NOT_CATEGORY + " ORDER BY SaleProduct.saleProductDateAdded DESC "

        const val ACTIVE_CATEGORIES_QUERY = ALL_ACTIVE_QUERY_WITHOUT_LE_FILTER + ALL_ACTIVE_QUERY_WITH_SEARCH +
                AND_IS_CATEGORY

        const val ACTIVE_PRODUCTS_NOT_IN_CATEGORY_QUERY =
                ALL_ACTIVE_QUERY_WITH_LE_FILTER + AND_IS_NOT_CATEGORY + AND_NOT_IN_CATEGORY

        const val ACTIVE_CATEGORIES_NOT_IN_CATEGORY_QUERY =
                ALL_ACTIVE_QUERY_WITH_LE_FILTER + AND_IS_CATEGORY + AND_NOT_IN_CATEGORY +
                        AND_NOT_COLLECTIONS


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProduct WHERE saleProductUid = :uid"

        const val FIND_BY_NAME_QUERY = "SELECT * FROM SaleProduct WHERE saleProductName = :name AND CAST(saleProductActive AS INTEGER) = 1"

        //INACTIVATE:
        const val INACTIVATE_QUERY = """
            UPDATE SaleProduct SET saleProductActive = 0
            , saleProductLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1)
            WHERE  saleProductUid = :uid 
            """
    }



}
