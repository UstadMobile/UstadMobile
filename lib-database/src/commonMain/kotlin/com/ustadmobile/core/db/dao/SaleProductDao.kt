package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleDescWithSaleProductPicture
import com.ustadmobile.lib.db.entities.SaleNameWithImage
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductGroup.Companion.PRODUCT_GROUP_TYPE_PRODUCT

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

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWILive(): DoorLiveData<List<SaleNameWithImage>>

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWIList(): List<SaleNameWithImage>

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract suspend fun findAllActiveSNWIAsync() : List<SaleNameWithImage>

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWIProvider(): DataSource.Factory<Int,SaleNameWithImage>

    //ITEMS(PRODUCTS):
    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWILive(): DoorLiveData<List<SaleNameWithImage>>

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWIList(): List<SaleNameWithImage>

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract suspend fun findAllActiveProductsSNWIAsync():List<SaleNameWithImage>

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWIProvider(): DataSource.Factory<Int,SaleNameWithImage>

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_PP)
    abstract fun findAllActiveProductsSNWIProviderWithPP(): DataSource.Factory<Int,SaleDescWithSaleProductPicture>

    //Products not existing already in Category
    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY)
    abstract fun findAllActiveProductsNotInCategorySNWIProvider(
            saleProductCategoryUid: Long): DataSource.Factory<Int,SaleNameWithImage>

    //CATEGORY:
    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWILive(): DoorLiveData<List<SaleNameWithImage>>

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWIList(): List<SaleNameWithImage>

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract suspend fun findAllActiveCategoriesSNWIAsync():List<SaleNameWithImage>

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWIProvider(): DataSource.Factory<Int,SaleNameWithImage>

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_PP)
    abstract fun findAllActiveCategoriesSNWIProviderWithPP(): DataSource.Factory<Int, SaleDescWithSaleProductPicture>

    //Categories not existing already in Category
    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY)
    abstract fun findAllActiveCategoriesNotInCategorySNWIProvider(
            saleProductCategoryUid: Long
    ): DataSource.Factory<Int, SaleNameWithImage>

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


        //FIND ALL ACTIVE

        const val ALL_ACTIVE_QUERY = "SELECT * FROM SaleProduct WHERE saleProductActive = 1"

        const val ALL_ACTIVE_CATEGORY_QUERY = "$ALL_ACTIVE_QUERY AND SaleProductCategory = 1"


        const val ALL_ACTIVE_NAME_WITH_IMAGE_QUERY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " +
                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0 " +
                " WHERE saleProductActive = 1 "

        const  val ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY =
                "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0  " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 0"

        const  val ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_PP =
                "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.* , " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0 " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 0"

        const val ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0  " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 0 " +
                "  AND SaleProduct.saleProductUid NOT IN (Select SaleProduct.saleProductUid FROM SaleProductParentJoin " +
                "  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid " +
                "  WHERE SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
                "  AND SaleProductParentJoin.saleProductParentJoinActive = 1 AND SaleProduct.saleProductActive = 1) " +
                " AND SaleProduct.saleProductUid != :saleProductCategoryUid "

        const val ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0  " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 1"

        const val ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_PP = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.* , " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0  " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 1"

        const val ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid AND SaleProductPicture.saleProductPictureIndex = 0  " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 1 " +
                "  AND SaleProduct.saleProductUid NOT IN (Select SaleProduct.saleProductUid FROM SaleProductParentJoin " +
                "  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid " +
                "  WHERE SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
                "  AND SaleProductParentJoin.saleProductParentJoinActive = 1 AND SaleProduct.saleProductActive = 1) " +
                " AND SaleProduct.saleProductUid != :saleProductCategoryUid "


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleProduct WHERE saleProductUid = :uid"

        const val FIND_BY_NAME_QUERY = "SELECT * FROM SaleProduct WHERE saleProductName = :name AND saleProductActive = 1"

        //INACTIVATE:

        const val INACTIVATE_QUERY = "UPDATE SaleProduct SET saleProductActive = 0 WHERE saleProductUid = :uid"
    }



}
