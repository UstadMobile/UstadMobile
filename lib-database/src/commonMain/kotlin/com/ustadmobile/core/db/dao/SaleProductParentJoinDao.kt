package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.db.dao.SaleDao.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleProductParentJoinDao : BaseDao<SaleProductParentJoin> {


    /**
     * Find all SaleProduct a given SaleProduct is a child of.
     * @param childSaleProductUid   The child sale product uid
     * @param resultListCallback    Return callback of list of SaleProduct
     */
    @Query("SELECT SaleProduct.* " +
            "FROM SaleProductParentJoin LEFT JOIN SaleProduct ON " +
            " SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
            " WHERE SaleProductParentJoin.saleProductParentJoinChildUid = :childSaleProductUid")
    abstract suspend fun findAllJoinByChildSaleProductAsync(childSaleProductUid: Long):List<SaleProduct>

    //Find all categories selected for a sale product
    @Query("SELECT CASE WHEN (SELECT COUNT(*) FROM SaleProductParentJoin " +
            " WHERE SaleProductParentJoin.saleProductParentJoinChildUid = :saleProductUid " +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = SaleProduct.saleProductUid " +
            " AND CAST(SaleProductParentJoin.saleProductParentJoinActive AS INTEGER) = 1 ) " +
            " > 0 THEN 1 ELSE 0 END AS isSelected, " +
            " SaleProduct.* " +
            "FROM SaleProduct WHERE SaleProduct.saleProductCategory = 1 " +
            " AND CAST(SaleProduct.saleProductActive AS INTEGER) = 1 AND SaleProduct.saleProductUid != :saleProductUid")
    abstract fun findAllSelectedCategoriesForSaleProductProvider(
            saleProductUid: Long): DataSource.Factory<Int,SaleProductSelected>

    @Query(QUERY_SELECT_ALL_SALE_PRODUCT + SEARCH_WHERE +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
            " AND CAST(child.saleProductCategory AS INTEGER) = 0 " + QUERY_SORT_BY_NAME_ASC )
    abstract fun findAllItemsInACategoryByNameAsc(leUid: Long, saleProductCategoryUid: Long, query: String): DataSource.Factory<Int,SaleProduct>

    @Query(QUERY_SELECT_ALL_SALE_PRODUCT + SEARCH_WHERE +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
            " AND CAST(child.saleProductCategory AS INTEGER) = 0 " + QUERY_SORT_BY_NAME_DESC )
    abstract fun findAllItemsInACategoryByNameDesc(leUid: Long, saleProductCategoryUid: Long, query: String): DataSource.Factory<Int,SaleProduct>


    fun sortAndFindAllItemsInACategory(leUid: Long, sort: Int, saleProductCategoryUid: Long, query: String)
            : DataSource.Factory<Int, SaleProduct>{
        when(sort){
            SORT_ORDER_NAME_ASC -> return findAllItemsInACategoryByNameAsc(leUid, saleProductCategoryUid, query)
            SORT_ORDER_NAME_DESC -> return findAllItemsInACategoryByNameDesc(leUid, saleProductCategoryUid, query)
        }
        return findAllItemsInACategoryByNameAsc(leUid, saleProductCategoryUid, query)
    }

    @Query(QUERY_SELECT_ALL_SALE_PRODUCT + SEARCH_WHERE +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
            " AND CAST(child.saleProductCategory AS INTEGER) = 1 ")
    abstract fun findAllCategoriesInACategoryByNameAsc(leUid: Long, saleProductCategoryUid: Long, query: String): DataSource.Factory<Int,SaleProduct>

    @Query(QUERY_SELECT_ALL_SALE_PRODUCT + SEARCH_WHERE +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
            " AND CAST(child.saleProductCategory AS INTEGER) = 1 ")
    abstract fun findAllCategoriesInACategoryByNameDesc(leUid: Long, saleProductCategoryUid: Long, query: String): DataSource.Factory<Int,SaleProduct>


    fun sortAndFindAllCategoriesInACategory(leUid: Long, sort: Int, saleProductCategoryUid: Long, query: String)
            :DataSource.Factory<Int, SaleProduct>{
        when(sort){
            SORT_ORDER_NAME_ASC -> return findAllCategoriesInACategoryByNameAsc(leUid, saleProductCategoryUid, query)
            SORT_ORDER_NAME_DESC -> return findAllCategoriesInACategoryByNameDesc(leUid, saleProductCategoryUid, query)
        }
        return findAllCategoriesInACategoryByNameAsc(leUid, saleProductCategoryUid, query)
    }


    @Query(QUERY_SELECT_ALL_SALE_PRODUCT + QUERY_WHERE_SEARCH +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = " +
            "   (SELECT SaleProduct.saleProductUid FROM SaleProduct " +
            "   WHERE SaleProduct.saleProductName = 'Collections' " +
            "   ORDER BY saleProductDateAdded ASC LIMIT 1) " +
            " " + QUERY_SORT_BY_NAME_ASC )
    abstract fun findAllItemsAndCategoriesInCollection(leUid: Long, query:String): DataSource.Factory<Int, SaleProduct>

    @Query(QUERY_SELECT_ALL_SALE_PRODUCT + QUERY_WHERE_SEARCH +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = " +
            "   (SELECT SaleProduct.saleProductUid FROM SaleProduct " +
            "   WHERE SaleProduct.saleProductName = 'Collections' " +
            "   ORDER BY saleProductDateAdded ASC LIMIT 1) " +
            " AND CAST(child.saleProductCategory AS INTEGER) = 1 ")
    abstract fun findAllCategoriesInCollection(leUid: Long, query: String): DataSource.Factory<Int, SaleProduct>

    @Query("SELECT * FROM SaleProductParentJoin WHERE " +
            " SaleProductParentJoin.saleProductParentJoinParentUid = :parentUid AND " +
            " SaleProductParentJoin.saleProductParentJoinChildUid = :childUid ")
    abstract suspend fun findByChildAndParentUid(childUid: Long, parentUid: Long)
            :SaleProductParentJoin?


    suspend fun createJoin(childProductUid: Long, parentProductUid: Long, activate: Boolean) {

        //1. Find existing mapping
        val result = findByChildAndParentUid(childProductUid, parentProductUid)
        if (result != null) {
            //Exists
            if (result.saleProductParentJoinActive != activate) {
                // Is not active
                result.saleProductParentJoinActive = activate
                update(result)
            } else {
                //Exists but is already set. Ignore
            }
        } else {
            //Create new with activate set
            val npj = SaleProductParentJoin(childProductUid,
                    parentProductUid, activate)
            insert(npj)
        }
    }

    //Find Top
    @Query("SELECT Parent.* FROM SaleProductParentJoin " +
            "LEFT JOIN SaleProduct as Parent ON " +
            "   Parent.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
            " WHERE CAST(SaleProductParentJoinActive  AS INTEGER) = 1 " +
            "   AND (SELECT COUNT(*) FROM SaleProductParentJoin AS sp " +
            "       WHERE sp.saleProductParentJoinChildUid = Parent.saleProductUid ) = 0 " +
            " GROUP BY saleProductParentJoinParentUid")
    abstract suspend fun findTopSaleProductsAsync():List<SaleProduct>


    @Query("SELECT Parent.* FROM SaleProductParentJoin " +
            "LEFT JOIN SaleProduct as Parent ON " +
            "   Parent.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
            " WHERE CAST(SaleProductParentJoinActive  AS INTEGER) = 1 " +
            "   AND (SELECT COUNT(*) FROM SaleProductParentJoin AS sp " +
            "       WHERE sp.saleProductParentJoinChildUid = Parent.saleProductUid ) = 0 " +
            " GROUP BY saleProductParentJoinParentUid")
    abstract fun findTopSaleProductsLive():DoorLiveData<List<SaleProduct>>


    //Find categories in a category uid
    @Query("SELECT Child.* FROM SaleProductParentJoin " +
            "   LEFT JOIN SaleProduct as Parent " +
            "   ON Parent.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
            "   LEFT JOIN SaleProduct as Child " +
            "   ON Child.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid " +
            " WHERE CAST(SaleProductParentJoinActive AS INTEGER) = 1 AND " +
            " CAST(Child.saleProductActive AS INTEGER) = 1 " +
            "   AND CAST(Child.saleProductCategory AS INTEGER) = 1 " +
            " AND SaleProductParentJoinParentUid = :uid ")
    abstract suspend fun findAllChildProductTypesForUidAsync(uid: Long):List<SaleProduct>

    companion object {

        const val QUERY_SELECT_ALL_SALE_PRODUCT =
                """
                    SELECT child.* 
                    FROM SaleProductParentJoin 
                    LEFT JOIN Person as LE on LE.personUid = :leUid 
                    LEFT JOIN SaleProduct child ON child.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid 
                    LEFT JOIN SaleProduct parent ON parent.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid 
                    LEFT JOIN SaleProductPicture productPicture ON productPicture.saleProductPictureSaleProductUid = child.saleProductUid
                    WHERE CAST(SaleProductParentJoin.saleProductParentJoinActive AS INTEGER) = 1
                    AND CAST(child.saleProductActive AS INTEGER) = 1 
                    AND (CAST(LE.admin AS INTEGER) = 1 OR child.saleProductPersonAdded = LE.personUid)
                    AND productPicture.saleProductPictureIndex = 
                    (SELECT MAX(SaleProductPicture.saleproductpictureIndex) from saleproductpicture 
                    where SaleProductPicture.saleProductPictureSaleProductUid = child.saleProductUid ) 
                """

        const val SEARCH_WHERE =
                """ AND (lower(child.saleProductName) like :query OR
                         lower(child.saleProductNameDari) like :query OR 
                        lower(child.saleProductNamePashto) like :query )"""

        const val QUERY_WHERE_SEARCH = " AND parent.saleProductName LIKE :query "

        const val QUERY_SORT_BY_NAME_ASC = " ORDER BY saleProductName ASC "

        const val QUERY_SORT_BY_NAME_DESC = " ORDER BY saleProductName DESC "
    }



}
