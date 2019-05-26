package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_PRODUCT;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN )
@UmRepository
public abstract class SaleProductDao implements SyncableDao<SaleProduct, SaleProductDao> {

    //INSERT

    @UmInsert
    public abstract void insertAsync(SaleProduct entity, UmCallback<Long> insertCallback);


    //FIND ALL ACTIVE

    public static final String ALL_ACTIVE_QUERY =
            "SELECT * FROM SaleProduct WHERE saleProductActive = 1";

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmLiveData<List<SaleProduct>> findAllActiveLive();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract List<SaleProduct> findAllActiveList();

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract void findAllActiveAsync(UmCallback<List<SaleProduct>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_QUERY)
    public abstract UmProvider<SaleProduct> findAllActiveProvider();

    public static final String ALL_ACTIVE_CATEGORY_QUERY = ALL_ACTIVE_QUERY +
            " AND SaleProductCategory = 1";

    //FIND ALL CATEGORIES

    @UmQuery(ALL_ACTIVE_CATEGORY_QUERY)
    public abstract void findAllCategoriesAsync(UmCallback<List<SaleProduct>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_CATEGORY_QUERY)
    public abstract UmProvider<SaleProduct> findAllCateogoriesProvider();


    public static final String ALL_ACTIVE_NAME_WITH_IMAGE_QUERY =
            "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
            " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
            " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                    PRODUCT_GROUP_TYPE_PRODUCT + " as type " +
            " FROM SaleProduct " +
            "  LEFT JOIN SaleProductPicture on " +
            " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
            " WHERE saleProductActive = 1 ";

    private static final String ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY =
            "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                    " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                    " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                    PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this
                    " FROM SaleProduct " +
                    "  LEFT JOIN SaleProductPicture on " +
                    " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
                    " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 0";

    private static final String ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY =
            "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
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
                    " AND SaleProduct.saleProductUid != :saleProductCategoryUid ";

    private static final String ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY =
            "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
                    " 0 as productGroupUid, SaleProduct.saleProductUid as productUid," +
                    " SaleProductPicture.saleProductPictureUid as pictureUid, " +
                    PRODUCT_GROUP_TYPE_PRODUCT + " as type " + //kinda irrelevant in new way TODO: check this
                    " FROM SaleProduct " +
                    "  LEFT JOIN SaleProductPicture on " +
                    " SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid " +
                    " WHERE saleProductActive = 1 AND SaleProduct.saleProductCategory = 1";

    private static final String ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY =
            "SELECT SaleProduct.saleProductName as name, SaleProduct.saleProductDesc as description, " +
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
                    " AND SaleProduct.saleProductUid != :saleProductCategoryUid ";

    @UmQuery(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    public abstract UmLiveData<List<SaleNameWithImage>> findAllActiveSNWILive();

    @UmQuery(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    public abstract List<SaleNameWithImage> findAllActiveSNWIList();

    @UmQuery(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    public abstract void findAllActiveSNWIAsync(UmCallback<List<SaleNameWithImage>> allActiveCallback);

    @UmQuery(ALL_ACTIVE_NAME_WITH_IMAGE_QUERY)
    public abstract UmProvider<SaleNameWithImage> findAllActiveSNWIProvider();

    //ITEMS(PRODUCTS):
    @UmQuery(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    public abstract UmLiveData<List<SaleNameWithImage>> findAllActiveProductsSNWILive();

    @UmQuery(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    public abstract List<SaleNameWithImage> findAllActiveProductsSNWIList();

    @UmQuery(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    public abstract void findAllActiveProductsSNWIAsync(UmCallback<List<SaleNameWithImage>> allActiveCallback);

    @UmQuery(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY)
    public abstract UmProvider<SaleNameWithImage> findAllActiveProductsSNWIProvider();

    //Products not existing already in Category
    @UmQuery(ALL_PRODUCTS_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY)
    public abstract UmProvider<SaleNameWithImage> findAllActiveProductsNotInCategorySNWIProvider(
            long saleProductCategoryUid);

    //CATEGORY:
    @UmQuery(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    public abstract UmLiveData<List<SaleNameWithImage>> findAllActiveCategoriesSNWILive();

    @UmQuery(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    public abstract List<SaleNameWithImage> findAllActiveCategoriesSNWIList();

    @UmQuery(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    public abstract void findAllActiveCategoriesSNWIAsync(UmCallback<List<SaleNameWithImage>> allActiveCallback);

    @UmQuery(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY)
    public abstract UmProvider<SaleNameWithImage> findAllActiveCategoriesSNWIProvider();

    //Categories not existing already in Category
    @UmQuery(ALL_CATEGORIES_NAME_WITH_IMAGE_QUERY_NOT_IN_CATEGORY)
    public abstract UmProvider<SaleNameWithImage> findAllActiveCategoriesNotInCategorySNWIProvider(
            long saleProductCategoryUid
    );


    //LOOK UP

    public static final String FIND_BY_UID_QUERY =
            "SELECT * FROM SaleProduct WHERE saleProductUid = :uid";

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract SaleProduct findByUid(long uid);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract void findByUidAsync(long uid, UmCallback<SaleProduct> findByUidCallback);

    @UmQuery(FIND_BY_UID_QUERY)
    public abstract UmLiveData<SaleProduct> findByUidLive(long uid);

    public static final String FIND_BY_NAME_QUERY =
        "SELECT * FROM SaleProduct WHERE saleProductName = :name AND saleProductActive = 1";

    @UmQuery(FIND_BY_NAME_QUERY)
    public abstract SaleProduct findByName(String name);

    @UmQuery(FIND_BY_NAME_QUERY)
    public abstract void findByNameAsync(String name, UmCallback<SaleProduct> findByUidCallback);

    @UmQuery(FIND_BY_NAME_QUERY)
    public abstract UmLiveData<SaleProduct> findByNameLive(String name);

    //INACTIVATE:

    public static final String INACTIVATE_QUERY =
            "UPDATE SaleProduct SET saleProductActive = 0 WHERE saleProductUid = :uid";
    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntity(long uid);

    @UmQuery(INACTIVATE_QUERY)
    public abstract void inactivateEntityAsync(long uid, UmCallback<Integer> inactivateCallback);


    //UPDATE:

    @UmUpdate
    public abstract void updateAsync(SaleProduct entity, UmCallback<Integer> updateCallback);

}
