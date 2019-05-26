package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductParentJoin;
import com.ustadmobile.lib.db.entities.SaleProductSelected;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition =
        RoleDao.SELECT_ACCOUNT_IS_ADMIN )
@UmRepository
public abstract class SaleProductParentJoinDao implements SyncableDao<SaleProductParentJoin, SaleProductParentJoinDao> {

    /**
     * Find all SaleProduct a given SaleProduct is a child of.
     * @param childSaleProductUid   The child sale product uid
     * @param resultListCallback    Return callback of list of SaleProduct
     */
    @UmQuery("SELECT SaleProduct.* FROM SaleProductParentJoin LEFT JOIN SaleProduct ON " +
            " SaleProduct.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
            " WHERE SaleProductParentJoin.saleProductParentJoinChildUid = :childSaleProductUid")
    public abstract void findAllJoinByChildSaleProductAsync(long childSaleProductUid,
                                                UmCallback<List<SaleProduct>> resultListCallback);

    //Find all categories selected for a sale product
    @UmQuery("SELECT CASE WHEN (SELECT COUNT(*) FROM SaleProductParentJoin " +
            " WHERE SaleProductParentJoin.saleProductParentJoinChildUid = :saleProductUid " +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = SaleProduct.saleProductUid " +
            " AND SaleProductParentJoin.saleProductParentJoinActive = 1) " +
            " > 0 THEN 1 ELSE 0 END AS selected, " +
            " SaleProduct.* " +
            "FROM SaleProduct WHERE SaleProduct.saleProductCategory = 1 AND SaleProduct.saleProductActive = 1")
    public abstract UmProvider<SaleProductSelected> findAllSelectedCategoriesForSaleProductProvider(
            long saleProductUid);

    public static final String QUERY_SELECT_ALL_SALE_PRODUCT =
            "SELECT child.saleProductName as name, child.saleProductDesc as description, productPicture.saleProductPictureUid as pictureUid, " +
                    " '' as type, child.saleProductUid as productUid, parent.saleProductUid as productGroupUid  " +
                    " FROM SaleProductParentJoin " +
                    " LEFT JOIN SaleProduct child ON child.saleProductUid = SaleProductParentJoin.saleProductParentJoinChildUid " +
                    " LEFT JOIN SaleProduct parent ON parent.saleProductUid = SaleProductParentJoin.saleProductParentJoinParentUid " +
                    " LEFT JOIN SaleProductPicture productPicture ON productPicture.saleProductPictureSaleProductUid = child.saleProductUid " +
                    " WHERE SaleProductParentJoin.saleProductParentJoinActive = 1 AND child.saleProductActive = 1 ";

    @UmQuery(QUERY_SELECT_ALL_SALE_PRODUCT +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
            " AND child.saleProductCategory = 0 ")
    public abstract UmProvider<SaleNameWithImage> findAllItemsInACategory(long saleProductCategoryUid);

    @UmQuery(QUERY_SELECT_ALL_SALE_PRODUCT +
            " AND SaleProductParentJoin.saleProductParentJoinParentUid = :saleProductCategoryUid " +
            " AND child.saleProductCategory = 1 ")
    public abstract UmProvider<SaleNameWithImage> findAllCategoriesInACategory(long saleProductCategoryUid);




    @UmQuery("SELECT * FROM SaleProductParentJoin WHERE " +
            " SaleProductParentJoin.saleProductParentJoinParentUid = :parentUid AND " +
            " SaleProductParentJoin.saleProductParentJoinChildUid = :childUid ")
    public abstract void findByChildAndParentUid(long childUid, long parentUid,
                                                 UmCallback<SaleProductParentJoin> resultCallback);


    public void createJoin(long childProductUid, long parentProductUid, boolean activate){

        //1. Find existing mapping
        findByChildAndParentUid(childProductUid, parentProductUid, new UmCallback<SaleProductParentJoin>() {
            @Override
            public void onSuccess(SaleProductParentJoin result) {
                if(result != null){
                    //Exists
                    if(result.isSaleProductParentJoinActive() != activate){
                        // Is not active
                        result.setSaleProductParentJoinActive(activate);
                        update(result);
                    }else{
                        //Exists but is already set. Ignore
                    }
                }else{
                    //Create new with activate set
                    SaleProductParentJoin npj = new SaleProductParentJoin(childProductUid,
                            parentProductUid, activate);
                    insert(npj);
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

}
