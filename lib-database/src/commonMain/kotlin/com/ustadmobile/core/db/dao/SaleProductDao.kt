package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleNameWithImage
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductGroup.Companion.PRODUCT_GROUP_TYPE_PRODUCT

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleProductDao : SyncableDao<SaleProduct, SaleProductDao> {

    //INSERT


    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): UmLiveData<List<SaleProduct>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<SaleProduct>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveAsync(allActiveCallback: UmCallback<List<SaleProduct>>)

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): UmProvider<SaleProduct>

    //FIND ALL CATEGORIES

    @Query(ALL_ACTIVE_CATEGORY_QUERY)
    abstract fun findAllCategoriesAsync(allActiveCallback: UmCallback<List<SaleProduct>>)

    @Query(ALL_ACTIVE_CATEGORY_QUERY)
    abstract fun findAllCateogoriesProvider(): UmProvider<SaleProduct>

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWILive(): UmLiveData<List<SaleNameWithImage>>

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWIList(): List<SaleNameWithImage>

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWIAsync(allActiveCallback: UmCallback<List<SaleNameWithImage>>)

    @Query(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveSNWIProvider(): UmProvider<SaleNameWithImage>

    //ITEMS(PRODUCTS):
    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWILive(): UmLiveData<List<SaleNameWithImage>>

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWIList(): List<SaleNameWithImage>

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWIAsync(allActiveCallback: UmCallback<List<SaleNameWithImage>>)

    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveProductsSNWIProvider(): UmProvider<SaleNameWithImage>

    //Products not existing already in Category
    @Query(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY)
    abstract fun findAllActiveProductsNotInCategorySNWIProvider(
            saleProductCategoryUid: Long): UmProvider<SaleNameWithImage>

    //CATEGORY:
    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWILive(): UmLiveData<List<SaleNameWithImage>>

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWIList(): List<SaleNameWithImage>

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWIAsync(allActiveCallback: UmCallback<List<SaleNameWithImage>>)

    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    abstract fun findAllActiveCategoriesSNWIProvider(): UmProvider<SaleNameWithImage>

    //Categories not existing already in Category
    @Query(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY)
    abstract fun findAllActiveCategoriesNotInCategorySNWIProvider(
            saleProductCategoryUid: Long
    ): UmProvider<SaleNameWithImage>

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(uid: Long): SaleProduct

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(uid: Long): SaleProduct

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): UmLiveData<SaleProduct>

    @Query(FIND_BY_NAME_QUERY)
    abstract fun findByName(name: String): SaleProduct

    @Query(FIND_BY_NAME_QUERY)
    abstract suspend fun findByNameAsync(name: String):SaleProduct

    @Query(FIND_BY_NAME_QUERY)
    abstract fun findByNameLive(name: String): UmLiveData<SaleProduct>

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntity(uid: Long)

    @Query(INACTIVATE_QUERY)
    abstract suspend fun inactivateEntityAsync(uid: Long):Int


    //UPDATE:

    @Update
    abstract suspend fun updateAsync(entity: SaleProduct):Int

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
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
                " WHERE saleProductActive = 1 "

        const  val ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 0"

        const val ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
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
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
                " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 1"

        const val ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY = "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this

                " FROM SaleProduct " +
                "  LEFT JOIN SaleProductPicture on " +
                " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
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
